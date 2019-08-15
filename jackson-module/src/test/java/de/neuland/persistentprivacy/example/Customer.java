package de.neuland.persistentprivacy.example;

import de.neuland.persistentprivacy.annotations.PersonalData;
import de.neuland.persistentprivacy.annotations.Pseudonymized;

class Customer {

    @Pseudonymized
    private String emailAddress;

    private String encodedPassword;

    @PersonalData
    private String firstName;

    @PersonalData
    private String lastName;

    public Customer() {

    }

    Customer(String emailAddress, String encodedPassword, String firstName, String lastName) {
        this.emailAddress = emailAddress;
        this.encodedPassword = encodedPassword;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }

    public void setEncodedPassword(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
