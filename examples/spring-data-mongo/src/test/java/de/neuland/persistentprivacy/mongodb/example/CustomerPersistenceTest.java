package de.neuland.persistentprivacy.mongodb.example;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;


@SpringBootTest
class CustomerPersistenceTest {

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer = new Customer("max@mustermann.de", "Max", "Mustermann");

    @Test
    void shouldSave() {


        Customer save = customerRepository.save(customer);
        System.out.println(save);

        Optional<Customer> byId = customerRepository.findById(save.getId());
        Assertions.assertThat(byId).isPresent();

    }
}
