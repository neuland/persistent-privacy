package de.neuland.persistentprivacy.jpa;

import de.neuland.persistentprivacy.crypto.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Configuration
public class PersistentPrivacyConfiguration {

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
    public CryptoService cryptoService() {
        return new DefaultCryptoService(keyRepository);
    }

}
