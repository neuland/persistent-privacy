package de.neuland.persistentprivacy.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
    List<Customer> findByEmail(String email);

    @Query(value = "SELECT c.firstName from Customer AS c WHERE c.id= :id")
    String getFirstnameById(Long id);

}
