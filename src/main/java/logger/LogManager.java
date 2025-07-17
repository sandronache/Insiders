package main.java.logger;

import java.util.ArrayList;
import java.util.List;

public class LogManager {
    private static final LogManager instance = new LogManager();

    private final List<ILogger> loggers = new ArrayList<>();

    public static LogManager getInstance() {
        return instance;
    }

    public void addLogger(ILogger logger) {
        loggers.add(logger);
    }

    public void removeLogger(ILogger logger) {
        loggers.remove(logger);
    }

    public void logDebug(String message) {
        for (ILogger logger : loggers) {
            logger.logDebug(message);
        }
    }

    public void logInfo(String message) {
        for (ILogger logger : loggers) {
            logger.logInfo(message);
        }
    }

    public void logWarning(String message) {
        for (ILogger logger : loggers) {
            logger.logWarning(message);
        }
    }

    public void logError(String message) {
        for (ILogger logger : loggers) {
            logger.logError(message);
        }
    }

    public void logFatal(String message) {
        for (ILogger logger : loggers) {
            logger.logFatal(message);
        }
    }
}
