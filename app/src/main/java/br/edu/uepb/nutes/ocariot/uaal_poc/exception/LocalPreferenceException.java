package br.edu.uepb.nutes.ocariot.uaal_poc.exception;

public class LocalPreferenceException extends RuntimeException {
    public LocalPreferenceException() {
    }

    public LocalPreferenceException(String message) {
        super(message);
    }

    public LocalPreferenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
