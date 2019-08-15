package de.neuland.persistentprivacy.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import de.neuland.persistentprivacy.annotations.PersonalData;
import de.neuland.persistentprivacy.crypto.CryptoService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PersonalDataEncryptionModule extends SimpleModule {

    static final String ENCRYPTED_FIELD_NAME = "$personal_data";
    private final CryptoService cryptoService;

    public PersonalDataEncryptionModule(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.addDeserializers(new EncryptedBeanDeserializer(cryptoService));
        context.addBeanSerializerModifier(new BeanSerializerModifier() {
            @Override
            public java.util.List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {

                List<BeanPropertyWriter> requiringEncryption = new ArrayList<>();

                List<BeanPropertyWriter> result = beanProperties.stream()
                        .filter(bpw -> {
                            boolean isPersonalData = bpw.getAnnotation(PersonalData.class) != null;
                            if (isPersonalData) {
                                requiringEncryption.add(bpw);
                            }
                            return !isPersonalData;
                        })
                        .collect(Collectors.toList());

                if (requiringEncryption.isEmpty()) {
                    return beanProperties;
                }

                result.add(
                        new EncryptedBeanPropertyWriter(requiringEncryption, cryptoService)
                );

                return result;
            }
        });
    }


}
