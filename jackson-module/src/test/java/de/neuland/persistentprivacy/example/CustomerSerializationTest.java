package de.neuland.persistentprivacy.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.neuland.persistentprivacy.jackson.PersonalDataEncryptionModule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomerSerializationTest {

    private ObjectMapper privacyProtectingMapper;
    private ObjectMapper standardMapper;

    private Customer customer = new Customer("max.mustermann@example.com", "hashedpasswd", "Max","Mustermann");

    @BeforeEach
    void setUp() {

        standardMapper = new ObjectMapper();

        privacyProtectingMapper = new ObjectMapper()
                .registerModule(new PersonalDataEncryptionModule(new NoopCryptoService()));


    }

    @Test
    void smokeTest() throws JsonProcessingException {
        System.out.println(standardMapper.writerWithDefaultPrettyPrinter().writeValueAsString(customer));
        System.out.println(privacyProtectingMapper.writerWithDefaultPrettyPrinter().writeValueAsString(customer));
    }

    @Test
    void shouldDeserialize() throws JsonProcessingException {
        String json = privacyProtectingMapper.writerWithDefaultPrettyPrinter().writeValueAsString(customer);
        System.out.println(json);
        Customer restored = privacyProtectingMapper.readValue(json, Customer.class);

        Assertions.assertThat(restored).isEqualToComparingFieldByField(customer);
    }
}
