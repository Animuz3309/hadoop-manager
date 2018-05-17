package edu.scut.cs.hm.common.fc;

public class FbException extends RuntimeException {
    public FbException() {
    }

    public FbException(String message) {
        super(message);
    }

    public FbException(String message, Throwable cause) {
        super(message, cause);
    }

    public FbException(Throwable cause) {
        super(cause);
    }
}
