package org.atoko.call4code.entrado.model.request;

import lombok.Data;

@Data
public class PersonCreateRequest {


    private final ThreadLocal<String> lastName = new ThreadLocal<String>();
    private String firstName;
    private String pin;

    public PersonCreateRequest() {
    }

    public PersonCreateRequest(String firstName, String lastName, String pin) {

        this.firstName = firstName;
        this.lastName.set(lastName);
        this.pin = pin;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public String getPin() {
        return pin;
    }
}
