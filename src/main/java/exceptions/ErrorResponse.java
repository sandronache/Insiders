package main.java.exceptions;

import java.time.LocalDateTime;
import java.util.List;


public class ErrorResponse {
    private final boolean success;
    private final ErrorDetails error;
    private final String timestamp;
    private final String path;

    public ErrorResponse(String code, String message, List<FieldErrorDetail> details, String path) {
        this.success = false;
        this.error = new ErrorDetails(code, message, details);
        this.timestamp = LocalDateTime.now().toString();
        this.path = path;
    }

    public boolean isSuccess() {
        return success;
    }

    public ErrorDetails getError() {
        return error;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPath() {
        return path;
    }

    public record ErrorDetails(String code, String message, List<FieldErrorDetail> details) {
    }
}

