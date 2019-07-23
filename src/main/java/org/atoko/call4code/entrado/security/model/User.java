package org.atoko.call4code.entrado.security.model;

import lombok.Data;

import java.util.List;

@Data
public class User {
    static private List<Role> personRoles = List.of(Role.ROLE_PERSON);
    String id;
    String session;
    List<Role> roles;

    private User(String id, String session, List<Role> roles) {
        this.id = id;
        this.session = session;
        this.roles = roles;
    }

    static public User person(String id, String session) {
        if (session == null) {
            session = "";
        }

        return new User(id, session, personRoles);
    }

    public String getId() {
        return id;
    }

    public String getSession() {
        return session;
    }

    public List<Role> getRoles() {
        return roles;
    }

}
