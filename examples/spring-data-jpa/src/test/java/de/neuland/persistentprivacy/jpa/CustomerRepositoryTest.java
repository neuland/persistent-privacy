package de.neuland.persistentprivacy.jpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer = new Customer("hans@mustermann.de", "Hans", "Mustermann");

    @Test
    void shouldCryptPersonalDataOnSave() {
        Customer saved = customerRepository.save(customer);
        String firstnameById = customerRepository.getFirstnameById(saved.getId());
        assertThat(firstnameById)
                .isNotNull()
                .isNotEqualTo("Hans")
                .matches("_crypt:key:[a-zA-Z0-9+/=]*:[a-zA-Z0-9+/=]*");

    }

    @Test
    void shouldNotEncryptEntityOnSave() {
        Customer saved = customerRepository.save(customer);
        assertThat(saved.getFirstName()).isEqualTo("Hans");
        assertThat(customer.getFirstName()).isEqualTo("Hans");
    }

    @Test
    void shouldDecryptOnReload() {
        Customer saved = customerRepository.save(customer);

        Optional<Customer> MaybeReloaded = customerRepository.findById(saved.getId());

        assertThat(MaybeReloaded)
                .isNotEmpty()
                .hasValueSatisfying(c -> assertThat(c).isEqualToComparingFieldByField(customer));
    }

}
