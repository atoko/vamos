package org.atoko.call4code.entrado.model.identifiers;

import lombok.Data;

@Data
public class PersonIdentifier {
    public String sourceId;
    public String personId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersonIdentifier that = (PersonIdentifier) o;

        if (sourceId != null ? !sourceId.equals(that.sourceId) : that.sourceId != null) return false;
        return personId != null ? personId.equals(that.personId) : that.personId == null;
    }

    @Override
    public int hashCode() {
        int result = sourceId != null ? sourceId.hashCode() : 0;
        result = 31 * result + (personId != null ? personId.hashCode() : 0);
        return result;
    }

    public PersonIdentifier() {
    }

    public PersonIdentifier(String sourceId, String personId) {
        this.sourceId = sourceId;
        this.personId = personId;
    }

    public static PersonIdentifier empty = new PersonIdentifier();
}
