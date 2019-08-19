package de.neuland.persistentprivacy.example;

import de.neuland.persistentprivacy.annotations.PersonalData;
import de.neuland.persistentprivacy.annotations.Pseudonymized;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
class Customer {

    @Pseudonymized
    private String emailAddress;

    private String encodedPassword;

    @PersonalData
    private String firstName;

    @PersonalData
    private String lastName;

    Customer(String emailAddress, String encodedPassword, String firstName, String lastName) {
        this.emailAddress = emailAddress;
        this.encodedPassword = encodedPassword;
        this.firstName = firstName;
        this.lastName = lastName;
    }

}
