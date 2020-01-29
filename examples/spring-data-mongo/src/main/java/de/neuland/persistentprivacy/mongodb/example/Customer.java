package de.neuland.persistentprivacy.mongodb.example;

import de.neuland.persistentprivacy.annotations.PersonalData;
import de.neuland.persistentprivacy.annotations.Pseudonymized;
import org.springframework.data.annotation.Id;

public class Customer {
    @Id
    private String id;
    @Pseudonymized
    private String email;
    @PersonalData
    private String firstName;
    @PersonalData
    private String lastName;

    public Customer() {}

    Customer(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return String.format(
                "Customer[id=%s, firstName='%s', lastName='%s']",
                id, firstName, lastName);
    }

    String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
