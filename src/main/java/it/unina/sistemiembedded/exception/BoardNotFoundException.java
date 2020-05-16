package it.unina.sistemiembedded.exception;

public class BoardNotFoundException extends Exception {
    public BoardNotFoundException(String message) {
        super(message);
    }

    public BoardNotFoundException() {
    }
}
