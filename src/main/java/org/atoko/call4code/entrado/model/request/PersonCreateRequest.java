package org.atoko.call4code.entrado.model.request;

import lombok.Data;
import org.springframework.util.MultiValueMap;

@Data
public class PersonCreateRequest {


    private String lastName;
    private String firstName;
    private String pin;

    public PersonCreateRequest() {
    }

    public PersonCreateRequest(String firstName, String lastName, String pin) {

        this.firstName = firstName;
        this.lastName =lastName;
        this.pin = pin;
    }

    public PersonCreateRequest(MultiValueMap<String, String> formData) {
        firstName = formData.getFirst("first-name");
        lastName = formData.getFirst("last-name");
        pin = formData.getFirst("checkin-pin");
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPin() {
        return pin;
    }
}
