package it.unina.sistemiembedded.exception;

/**
 * Requested board was not found
 */
public class BoardNotFoundException extends Exception {
    public BoardNotFoundException(String message) {
        super(message);
    }

    public BoardNotFoundException() {
    }
}
