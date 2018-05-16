package edu.scut.cs.hm.common.validate;

/**
 * Exception which may be thrown when validation is failed.
 */
public class ValidityException extends RuntimeException {

    private transient final Validity validity;

    /**
     *
     * @param messagePart first part of message
     * @param validity
     */
    public ValidityException(String messagePart, Validity validity) {
        super(messagePart + validity.getMessage());
        this.validity = validity;
    }

    public Validity getValidity() {
        return validity;
    }
}

