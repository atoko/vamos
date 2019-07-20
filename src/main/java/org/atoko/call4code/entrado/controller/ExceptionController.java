package org.atoko.call4code.entrado.controller;

import org.atoko.call4code.entrado.exception.FrontendException;
import org.atoko.call4code.entrado.exception.ResponseCodeException;
import org.atoko.call4code.entrado.model.error.ErrorNode;
import org.atoko.call4code.entrado.model.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(value = ResponseCodeException.class)
    public ResponseEntity<ErrorResponse> defaultExceptionHandler(ResponseCodeException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ErrorResponse(
                        new ErrorNode(
                                ex.getCode(),
                                ex.getReason()
                        )
                ));
    }

    @ExceptionHandler(value = FrontendException.class)
    public String defaultExceptionHandler(FrontendException ex) {
        return "redirect:" + ex.getRedirect()  + "?";
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> defaultExceptionHandler(Exception ex) {
        Throwable cause = ex.getCause();
        String name = ex.getClass().getName();
        if (cause != null) {
            name = cause.getClass().getName();
        }
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT)
                .body(new ErrorResponse(
                        new ErrorNode(
                                name,
                                ex.getMessage()
                        )
                ));
    }
}
