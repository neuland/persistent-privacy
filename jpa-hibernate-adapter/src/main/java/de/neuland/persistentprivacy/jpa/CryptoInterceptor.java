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
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CryptoInterceptor extends EmptyInterceptor {

    private static final String PREFIX = "_crypt";
    @Autowired
    private CryptoService cryptoService;

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        return cryptPersonalDataFields(entity, state, propertyNames);
    }

    private boolean cryptPersonalDataFields(Object entity, Object[] state, String[] propertyNames) {
        List<String> personalDataPropertyNames = getPersonalDataPropertyNames(entity);
        for (int i = 0; i < propertyNames.length; i++) {
            if (personalDataPropertyNames.contains(propertyNames[i])) {
                byte[] bytes = toByteArray(state[i]);
                state[i] = serializeAsString(cryptoService.encrypt(bytes));
            }
        }
        return !personalDataPropertyNames.isEmpty();
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        return cryptPersonalDataFields(entity, currentState, propertyNames);
    }

    @Override
    public void postFlush(Iterator entities) {
        entities.forEachRemaining(this::restoreOriginalValues);
    }

    private void restoreOriginalValues(Object entity) {
        getPersonalDataProperties(entity)
                .forEach(f -> restoreOriginalValue(f, entity));
    }

    private void restoreOriginalValue(Field personalDataField, Object entity) {
        try {
            String crypted = (String) personalDataField.get(entity);
            CryptedData.tryDeserializeFromString(crypted)
                    .map(cryptoService::decrypt)
                    .map(plain -> new String(plain, StandardCharsets.UTF_8))
                    .ifPresent(plainText -> {
                        try {
                            personalDataField.set(entity, plainText);
                        } catch (IllegalAccessException e) {
                            throw new IllegalStateException("Cannot restore original value on " + entity.getClass() + "." + personalDataField.getName(), e);
                        }
                    });
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }

    }

    private String serializeAsString(CryptedData cryptedData) {
        return cryptedData.serializeAsString(PREFIX);
    }


    private byte[] toByteArray(Object o) {
        if (o instanceof String) {
            return ((String) o).getBytes(StandardCharsets.UTF_8);
        }
        throw new UnsupportedOperationException("Can only crypt String fields for JPA entities");
    }

    private List<String> getPersonalDataPropertyNames(Object entity) {
        return getPersonalDataProperties(entity)
                .stream()
                .map(Field::getName)
                .collect(Collectors.toList());
    }

    private List<Field> getPersonalDataProperties(Object entity) {
        return Arrays
                .stream(entity.getClass().getDeclaredFields())
                .filter(this::isPersonalDataField)
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toList());
    }

    private boolean isPersonalDataField(Field f) {
        boolean annotationPresent = f.getAnnotation(PersonalData.class) != null;
        if (annotationPresent) {
            if (!f.getType().equals(String.class)) {
                throw new UnsupportedOperationException("Can only encrypt String properties with JPA");
            }
        }
        return annotationPresent;
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
