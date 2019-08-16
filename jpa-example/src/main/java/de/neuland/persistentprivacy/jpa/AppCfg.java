package de.neuland.persistentprivacy.jpa;

import de.neuland.persistentprivacy.crypto.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

@Configuration
public class AppCfg {

    private KeyRepository keyRepository = new KeyRepository() {

        private String encodedKey = "SXrCuhJwJSbLif8+Ol6wwe5gqHYnM7H6DX2PmBMg8WM=";

        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        private Key key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

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
        return new AesGcmCryptoService(keyRepository);
    }

}
