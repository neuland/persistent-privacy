package de.neuland.persistentprivacy.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import de.neuland.persistentprivacy.crypto.CryptedData;
import de.neuland.persistentprivacy.crypto.CryptoService;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class EncryptedBeanPropertyWriter extends BeanPropertyWriter {
    private final List<BeanPropertyWriter> personalDataProperties;
    private final CryptoService cryptoService;
    private static final String DO_ENCRYPTION = "encrypt_personal_data";

    EncryptedBeanPropertyWriter(List<BeanPropertyWriter> personalDataProperties, CryptoService cryptoService) {
        this.personalDataProperties = personalDataProperties;
        this.cryptoService = cryptoService;
    }



    @Override
    public void serializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov) throws Exception {
        if (shouldEncrypt(prov)) {
            gen.writeFieldName(PersonalDataEncryptionModule.ENCRYPTED_FIELD_NAME);

            ObjectMapper codec = ((ObjectMapper) gen.getCodec()).copy();
            codec.setConfig(deactivateEncryptionForNestedPersonalDataAttributes(codec));

            CryptedData crypted = cryptoService.encrypt(data(bean, codec));
            prov.defaultSerializeValue(crypted, gen);
        } else {
            personalDataProperties.forEach(bpw -> delegateSerializeAsField(bean, gen, prov, bpw));
        }
    }

    private SerializationConfig deactivateEncryptionForNestedPersonalDataAttributes(ObjectMapper codec) {
        return codec.getSerializationConfig().withAttribute(DO_ENCRYPTION, false);
    }

    private boolean shouldEncrypt(SerializerProvider prov) {
        Object attr = prov.getConfig().getAttributes().getAttribute(DO_ENCRYPTION);
        return attr == null || (boolean) attr;
    }

    @SneakyThrows
    private void delegateSerializeAsField(Object bean, JsonGenerator gen, SerializerProvider prov, BeanPropertyWriter bpw) {
        bpw.serializeAsField(bean, gen, prov);
    }

    private byte[] data(Object bean, ObjectMapper codec) throws JsonProcessingException {

        Map<String, Object> asMap = personalDataProperties
                .stream()
                .collect(Collectors.toMap(BeanPropertyWriter::getName, bpw -> getProperty(bean, bpw)));

        String json = codec.writeValueAsString(asMap);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    @SneakyThrows
    private Object getProperty(Object bean, BeanPropertyWriter bpw) {
        return bpw.get(bean);
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    public void fixAccess(SerializationConfig config) {
        personalDataProperties.forEach(beanPropertyWriter -> beanPropertyWriter.fixAccess(config));
    }

    @Override
    public boolean hasSerializer() {
        return true;
    }

    @Override
    public String getName() {
        return PersonalDataEncryptionModule.ENCRYPTED_FIELD_NAME;
    }


}
