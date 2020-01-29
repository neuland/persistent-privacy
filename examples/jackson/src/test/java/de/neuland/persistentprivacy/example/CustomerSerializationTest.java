package de.neuland.persistentprivacy.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.proto.KeyTemplate;
import de.neuland.persistentprivacy.crypto.NoopCryptoService;
import de.neuland.persistentprivacy.crypto.TinkCryptoService;
import de.neuland.persistentprivacy.crypto.TinkKeysetRepository;
import de.neuland.persistentprivacy.jackson.PersonalDataEncryptionModule;
import org.apache.commons.codec.binary.Hex;
import org.assertj.core.api.Assertions;
import org.bouncycastle.crypto.generators.BCrypt;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerSerializationTest {

    private ObjectMapper privacyProtectingMapper;

    private Customer customer = new Customer("max.mustermann@example.com",
            "$2y$12$xrh9tvl5WHna89.Od21EfuLanukZFYszmpuyNJwNTdmfAmHdQZW4W",
            "Max",
            "Mustermann"
    );
    private static KeysetHandle keysetHandle;

    @BeforeAll
    static void beforeAll() throws Exception {
        AeadConfig.register();
        KeyTemplate keyTemplate = AeadKeyTemplates.AES128_GCM;
        keysetHandle = KeysetHandle.generateNew(keyTemplate);
    }

    @BeforeEach
    void setUp() {
        TinkCryptoService tinkCryptoService = new TinkCryptoService(new TinkKeysetRepository() {
            @Override
            public KeysetHandle defaultKeyset() {
                return keysetHandle;
            }

            @Override
            public KeysetHandle forKeyId(String keyId) {
                return keysetHandle;
            }
        });


        privacyProtectingMapper = new ObjectMapper()
                .registerModule(new PersonalDataEncryptionModule(tinkCryptoService));
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
        assertThat(restored.getFirstName()).isEqualTo(customer.getFirstName());
        assertThat(restored.getLastName()).isEqualTo(customer.getLastName());

    }
}
