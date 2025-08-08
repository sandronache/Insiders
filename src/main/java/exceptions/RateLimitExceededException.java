package main.java.exceptions;

public class RateLimitExceededException extends Exception {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
