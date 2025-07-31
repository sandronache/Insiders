package main.java.logger;

public class ConsoleLogger implements ILogger {

    @Override
    public void log(String message) {
        System.out.println(message);
    }

    @Override
    public void logDebug(String message) {
        System.out.println("DEBUG: " + message);
    }

    @Override
    public void logInfo(String message) {
        System.out.println("INFO: " + message);
    }

    @Override
    public void logWarning(String message) {
        System.out.println("WARNING: " + message);
    }

    @Override
    public void logError(String message) {
        System.err.println("ERROR: " + message);
    }

    @Override
    public void logFatal(String message) {
        System.err.println("FATAL: " + message);
    }
}
