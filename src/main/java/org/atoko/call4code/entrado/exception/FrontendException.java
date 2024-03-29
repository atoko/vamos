package org.atoko.call4code.entrado.exception;

public class FrontendException extends RuntimeException {
    private final String redirect;

    public FrontendException(Throwable e, String redirect) {
        super(e.getMessage(), e);

        if (redirect == null) {
            redirect = "/";
        }
        this.redirect = redirect;
    }

    public String getRedirect() {
        return redirect;
    }
}
