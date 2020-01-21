package de.neuland.persistentprivacy.mongodb.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.neuland.persistentprivacy.crypto.*;
import de.neuland.persistentprivacy.mongodb.PrivacyProtectionListener;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.MongoMappingEvent;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Configuration
public class ExampleConfiguration {

    private KeyRepository keyRepository = new KeyRepository() {
        byte[] testKeyData = Base64.getDecoder().decode("SXrCuhJwJSbLif8+Ol6wwe5gqHYnM7H6DX2PmBMg8WM=");
        private Key key = new SecretKeySpec(testKeyData, "AES");

        @Override
        public Key keyForName(String keyRef) {
            return key;
        }

        @Override
        public KeyData defaultAesKey() {
            return new KeyData("key",key);
        }
    };

    @Bean
    public ApplicationListener<MongoMappingEvent<?>> privacyProtectionListener() {
        CryptoService cryptoService = new ExampleCryptoService(keyRepository);
        ObjectMapper personalDataObjectMapper = new ObjectMapper();
        return new PrivacyProtectionListener(cryptoService, personalDataObjectMapper);
    }

}
