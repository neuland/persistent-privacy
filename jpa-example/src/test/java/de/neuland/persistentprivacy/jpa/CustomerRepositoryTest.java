package de.neuland.persistentprivacy.jpa;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


@SpringBootTest
class CustomerRepositoryTest {


    @Autowired
    private CustomerRepository customerRepository;


    @Test
    void name() {
        Customer customer = new Customer("hans@mustermann.de", "Hans", "Mustermann");
        customerRepository.save(customer);

        List<Customer> mustermaenner = customerRepository.findByEmail("hans@mustermann.de");
        Assertions.assertThat(mustermaenner).isNotEmpty();
    }
}
