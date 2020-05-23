package it.unina.sistemiembedded.exception;

/**
 * Board already exists in Server list
 */
public class BoardAlreadyExistsException extends Exception {
    public BoardAlreadyExistsException(String message) {
        super(message);
    }
}
