package org.insiders.backend.exceptions;

public class InvalidVoteTypeException extends IllegalArgumentException {
    public InvalidVoteTypeException(String message) {
        super(message);
    }
}
