package de.neuland.persistentprivacy.jpa;

import de.neuland.persistentprivacy.annotations.PersonalData;

import javax.persistence.*;

@Entity
public class Customer {

    @Id
     @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String email;
    @PersonalData
//    @Convert(converter = CryptoConverter.class)
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


}
