package org.atoko.call4code.entrado.model;

import lombok.Data;

@Data
public class PersonDetails {
    public String id = "";
    public String fname = "";
    public String lname = "";

    public PersonDetails(String fname, String lname, String id) {
        this.id = id;
        this.fname = fname;
        this.lname = lname;

    }

    public PersonDetails() {
    }

}
