package org.atoko.call4code.entrado.model.error;

public class ErrorNode {
    //    private List<ErrorNode> rootCause;
    private String code;
    private String description;

    public ErrorNode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
