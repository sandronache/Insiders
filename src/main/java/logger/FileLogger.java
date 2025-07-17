package main.java.logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class FileLogger implements ILogger {
    private final String logFile;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FileLogger(String logFile) {
        this.logFile = logFile;
    }

    @Override
    public void logDebug(String message) {
        writeToFile("DEBUG", message);
    }

    @Override
    public void logInfo(String message) {
        writeToFile("INFO", message);
    }

    @Override
    public void logWarning(String message) {
        writeToFile("WARNING", message);
    }

    @Override
    public void logError(String message) {
        writeToFile("ERROR", message);
    }

    @Override
    public void logFatal(String message) {
        writeToFile("FATAL", message);
    }

    private void writeToFile(String level, String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            LocalDateTime now = LocalDateTime.now();
            writer.println(now.format(formatter) + " " + level + ": " + message);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
}