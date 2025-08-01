package main.java.logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LogManager {
    private static final LogManager instance = new LogManager();
    private final List<ILogger> loggers = new CopyOnWriteArrayList<>();
    private final LoggerThread loggerThread = new LoggerThread();
    private LogManager() {
        loggerThread.start();
    }

    public static LogManager getInstance() {
        return instance;
    }

    public void addLogger(ILogger logger) {
        loggers.add(logger);
    }

    public void removeLogger(ILogger logger) {
        loggers.remove(logger);
    }

    private void log(String level, String message) {
        String formatted = "[" + level + "] " + message;
        loggerThread.log(formatted);
        // multiple logging outputs (file, console)
        for (ILogger logger : loggers) {
            logger.log(formatted);
        }
    }

    public void logDebug(String message) { log("DEBUG", message); }
    public void logInfo(String message) { log("INFO", message); }
    public void logWarning(String message) { log("WARNING", message); }
    public void logError(String message) { log("ERROR", message); }
    public void logFatal(String message) { log("FATAL", message); }
    public void shutdown() { loggerThread.shutdown(); }
}
