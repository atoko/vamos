package org.atoko.call4code.entrado.model.response;

import org.atoko.call4code.entrado.model.error.ErrorNode;

public class ErrorResponse {
    private ErrorNode error;

    public ErrorResponse(ErrorNode error) {
        this.error = error;
    }

    public ErrorNode getError() {
        return error;
    }

    public void setError(ErrorNode error) {
        this.error = error;
    }
}
