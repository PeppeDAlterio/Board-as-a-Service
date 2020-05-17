package it.unina.sistemiembedded.exception;

public class NotConnectedException extends RuntimeException {
    public NotConnectedException(String message) {
        super(message);
    }

    public NotConnectedException() {
    }
}
