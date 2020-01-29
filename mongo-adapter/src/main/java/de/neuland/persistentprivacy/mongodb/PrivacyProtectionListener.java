package de.neuland.persistentprivacy.mongodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.neuland.persistentprivacy.annotations.PersonalData;
import de.neuland.persistentprivacy.annotations.Pseudonymized;
import de.neuland.persistentprivacy.crypto.CryptedData;
import de.neuland.persistentprivacy.crypto.CryptoService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class PrivacyProtectionListener extends AbstractMongoEventListener<Object> {

    public static final String FIELD_NAME = "_personal_data";

    private CryptoService cryptoService;

    private ObjectMapper objectMapper;

    public PrivacyProtectionListener(CryptoService cryptoService,  ObjectMapper personalDataObjectMapper) {
        this.cryptoService = cryptoService;
        this.objectMapper = personalDataObjectMapper;
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<Object> event) {

        try {
            if (event.getDocument() != null && event.getSource() != null) {
                Map<String, Object> personalData = personalData(event.getSource());

                if (!personalData.isEmpty()) {
                    Document document = event.getDocument();
                    personalData.forEach((key, value) -> document.remove(key));

                    CryptedData cryptedData = cryptoService.encrypt(objectMapper.writeValueAsBytes(personalData));

                    document.append(FIELD_NAME, toDocument(cryptedData));
                    pseudonymize(event.getSource(), document);
                }
            }

        } catch (Exception e) {
            log.error("Error while trying to rewrite mongo document", e);
            throw new RuntimeException(e);
        }

        super.onBeforeSave(event);
    }

    private Document toDocument(CryptedData cryptedData) {
        Document doc = new Document();
        doc.append("data", cryptedData.getData());
        doc.append("iv", cryptedData.getIv());
        doc.append("keyRef", cryptedData.getKeyRef());
        return doc;

    }

    private Optional<CryptedData> fromDocument(Document maybeDocument) {
        return Optional.ofNullable(maybeDocument)
                .flatMap(document -> Optional.ofNullable((Document) document.get(FIELD_NAME)))
                .map(cryptDataDoc ->
                        CryptedData.builder()
                                .data(cryptDataDoc.getString("data"))
                                .iv(cryptDataDoc.getString("iv"))
                                .keyRef(cryptDataDoc.getString("keyRef"))
                                .build()
                );

    }

    private Map<String, Object> personalData(Object source) throws IllegalAccessException {
        Map<String, Object> personalData = new HashMap<>();

        Field[] fields = source.getClass().getDeclaredFields();
        for (var f : fields) {
            if (isPersonalData(f)) {
                f.setAccessible(true);
                Object value = f.get(source);
                // TODO also return mongo column name if deviating
                personalData.put(f.getName(), value);
            }
        }

        return personalData;
    }

    private void pseudonymize(Object source, Document to) throws IllegalAccessException {
        Field[] fields = source.getClass().getDeclaredFields();
        for (var f : fields) {
            if (isPseudonymizedData(f)) {
                f.setAccessible(true);
                Object value = f.get(source);
                // TODO also return mongo column name if deviating
                if ( value != null) {
                    String pseudonymized = cryptoService.pseudonymizeAsHex(value.toString().getBytes(StandardCharsets.UTF_8));
                    to.put(f.getName(), pseudonymized);
                }

            }
        }

    }

    private boolean isPersonalData(Field field) {
        return field.getAnnotation(PersonalData.class) != null || isPseudonymizedData(field);
    }

    private boolean isPseudonymizedData(Field field) {
        return field.getAnnotation(Pseudonymized.class) != null;
    }


    @Override
    public void onAfterLoad(AfterLoadEvent<Object> event) {
        super.onAfterLoad(event);
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<Object> event) {

        fromDocument(event.getDocument())
                .map(cryptoService::decrypt)
                .map(plainText -> deserializePersonalData(event, plainText))
                .ifPresent(personalData -> merge(personalData, event.getSource()));

        super.onAfterConvert(event);
    }

    @SneakyThrows
    private void merge(Object personalData, Object into) {
        for (Field f : personalData.getClass().getDeclaredFields()) {
            if (isPersonalData(f)) {
                f.setAccessible(true);
                Object personalValue = f.get(personalData);
                f.set(into, personalValue);
            }
        }
    }

    @SneakyThrows
    private Object deserializePersonalData(AfterConvertEvent<Object> event, byte[] plainText) {
        return objectMapper.readValue(plainText, event.getSource().getClass());
    }
}
