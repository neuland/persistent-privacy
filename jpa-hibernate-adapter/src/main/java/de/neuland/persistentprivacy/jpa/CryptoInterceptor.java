package de.neuland.persistentprivacy.jpa;

import de.neuland.persistentprivacy.annotations.PersonalData;
import de.neuland.persistentprivacy.crypto.CryptedData;
import de.neuland.persistentprivacy.crypto.CryptoService;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CryptoInterceptor extends EmptyInterceptor {

    private static final String PREFIX = "_crypt";
    @Autowired
    private CryptoService cryptoService;

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        List<String> personalDataPropertyNames = getPersonalDataPropertyNames(entity);
        for (int i = 0; i < propertyNames.length; i++) {
            if (personalDataPropertyNames.contains(propertyNames[i])) {
                byte[] bytes = toByteArray(state[i]);
                if (bytes != null) {
                    state[i] = serializeAsString(cryptoService.encrypt(bytes));
                }
            }
        }

        return true;
    }

    private String serializeAsString(CryptedData cryptedData) {
        return cryptedData.serializeAsString(PREFIX);
    }


    private byte[] toByteArray(Object o) {
        if (o instanceof String) {
            return ((String) o).getBytes(StandardCharsets.UTF_8);
        }
        // FIXME Handle
        return null;
    }


    private List<String> getPersonalDataPropertyNames(Object entity) {
        List<String> props = new ArrayList<>();
        for (Field f : entity.getClass().getDeclaredFields()) {
            if (f.getAnnotation(PersonalData.class) != null) {
                props.add(f.getName());
            }
        }


        return props;
    }

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        List<String> personalDataPropertyNames = getPersonalDataPropertyNames(entity);
        for (int i = 0; i < propertyNames.length; i++) {
            if (personalDataPropertyNames.contains(propertyNames[i])) {
                if (state[i] != null && state[i] instanceof String) {
                    String ciphertext = (String) state[i];

                    if (ciphertext.startsWith(PREFIX)) {
                        byte[] decrypted = cryptoService.decrypt(CryptedData.deserializeFromString(ciphertext));
                        state[i] = new String(decrypted, StandardCharsets.UTF_8);
                    }
                }
            }
        }

        return true;
    }


}
