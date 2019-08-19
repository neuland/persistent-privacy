package de.neuland.persistentprivacy.mongodb.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.neuland.persistentprivacy.crypto.CryptoService;
import de.neuland.persistentprivacy.crypto.NoopCryptoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExampleConfiguration {

    @Bean
    public CryptoService cryptoService() {
        return new NoopCryptoService();
    }

    @Bean
    public ObjectMapper personalDataObjectMapper() {
        return new ObjectMapper();
    }


}
