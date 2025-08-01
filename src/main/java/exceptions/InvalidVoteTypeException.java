package main.java.exceptions;

public class InvalidVoteTypeException extends RuntimeException {
    public InvalidVoteTypeException(String message) {
        super(message);
    }
}
