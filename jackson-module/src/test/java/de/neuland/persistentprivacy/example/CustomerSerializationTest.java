package de.neuland.persistentprivacy.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.neuland.persistentprivacy.crypto.NoopCryptoService;
import de.neuland.persistentprivacy.jackson.PersonalDataEncryptionModule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerSerializationTest {

    private ObjectMapper privacyProtectingMapper;

    private Customer customer = new Customer("max.mustermann@example.com", "hashedpasswd", "Max", "Mustermann");

    @BeforeEach
    void setUp() {
        privacyProtectingMapper = new ObjectMapper()
                .registerModule(new PersonalDataEncryptionModule(new NoopCryptoService()));
    }

    @Test
    void smokeTest() throws JsonProcessingException {
        String json = privacyProtectingMapper.writerWithDefaultPrettyPrinter().writeValueAsString(customer);
        assertThat(json).isNotNull()
                .doesNotContain("\"firstName\"")
                .doesNotContain("\"lastName\"")
                .contains("$personal_data")
                .contains("\"emailAddress\"")
                .doesNotContain("max.mustermann@example.com");
        System.out.println(json);
    }

    @Test
    void shouldDeserialize() throws JsonProcessingException {
        String json = privacyProtectingMapper.writerWithDefaultPrettyPrinter().writeValueAsString(customer);
        System.out.println(json);
        Customer restored = privacyProtectingMapper.readValue(json, Customer.class);

        assertThat(restored).isEqualToComparingFieldByField(customer);
        assertThat(restored.getEmailAddress()).isEqualTo("max.mustermann@example.com");

    }
}
