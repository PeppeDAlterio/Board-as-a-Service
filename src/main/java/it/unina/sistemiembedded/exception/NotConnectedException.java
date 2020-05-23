package it.unina.sistemiembedded.exception;

/**
 * Client not connected
 */
public class NotConnectedException extends RuntimeException {
    public NotConnectedException(String message) {
        super(message);
    }

    public NotConnectedException() {
    }
}
