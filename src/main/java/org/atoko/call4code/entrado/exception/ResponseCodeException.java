package org.atoko.call4code.entrado.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ResponseCodeException extends ResponseStatusException {
    String code;

    /**
     * Constructor with a response status.
     *
     * @param status the HTTP status (required)
     */
    public ResponseCodeException(HttpStatus status, String code) {
        super(status);
        this.code = code;
    }

    /**
     * Constructor with a response status and a reason to add to the exception
     * message as explanation.
     *
     * @param status the HTTP status (required)
     * @param reason the associated reason (optional)
     */
    public ResponseCodeException(HttpStatus status, String reason, String code) {
        super(status, reason);
        this.code = code;
    }

    /**
     * Constructor with a response status and a reason to add to the exception
     * message as explanation, as well as a nested exception.
     *
     * @param status the HTTP status (required)
     * @param reason the associated reason (optional)
     * @param cause  a nested exception (optional)
     */
    public ResponseCodeException(HttpStatus status, String reason, Throwable cause, String code) {
        super(status, reason, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
