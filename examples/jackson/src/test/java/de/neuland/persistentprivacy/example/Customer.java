package de.neuland.persistentprivacy.example;

import de.neuland.persistentprivacy.annotations.PersonalData;
import de.neuland.persistentprivacy.annotations.Pseudonymized;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
class Customer {
    @Pseudonymized
    private String emailAddress;
    private String encodedPassword;
    @PersonalData
    private String firstName;
    @PersonalData
    private String lastName;
}
