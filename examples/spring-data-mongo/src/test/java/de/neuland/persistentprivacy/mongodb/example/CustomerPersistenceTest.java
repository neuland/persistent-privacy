package de.neuland.persistentprivacy.mongodb.example;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import de.neuland.persistentprivacy.mongodb.PrivacyProtectionListener;
import org.bson.Document;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ContextConfiguration(initializers = {CustomerPersistenceTest.Initializer.class})
class CustomerPersistenceTest {

    @ClassRule
    public static GenericContainer mongo = new GenericContainer("mongo").withExposedPorts(27017);

    @BeforeAll
    static void setUp() {
        mongo.start();
    }

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MongoClient mongoClient;

    private Customer customer = new Customer("max@mustermann.de", "Max", "Mustermann");

    @Test
    void shouldEncryptOnSave() {
        Customer save = customerRepository.save(customer);

        Optional<Customer> byId = customerRepository.findById(save.getId());
        assertThat(byId).isPresent();

        FindIterable<Document> documents = mongoClient
                .getDatabase("test")
                .getCollection("customer")
                .find();

        for (Document d: documents) {
            assertThat(d.getString("firstName")).isNull();
            assertThat(d.getString("lastName")).isNull();

            assertThat(d.get(PrivacyProtectionListener.FIELD_NAME)).isNotNull().isInstanceOfSatisfying(Document.class, sub -> {
                assertThat(sub.getString("data")).isNotNull();
                assertThat(sub.getString("iv")).isNotNull();
                assertThat(sub.getString("keyRef")).isNotNull();
            });
        }

    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            String uri = "mongodb://" + mongo.getContainerIpAddress() + ":" + mongo.getMappedPort(27017) + "/test";
            TestPropertyValues.of("spring.data.mongodb.uri="+ uri).applyTo(configurableApplicationContext);
        }
    }

}
