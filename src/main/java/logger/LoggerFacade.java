package main.java.logger;

public class LoggerFacade {

    public static void debug(String message) {
        LogManager.getInstance().logDebug(message);
    }

    public static void info(String message) {
        LogManager.getInstance().logInfo(message);
    }

    public static void warning(String message) {
        LogManager.getInstance().logWarning(message);
    }

    public static void error(String message) {
        LogManager.getInstance().logError(message);
    }

    public static void fatal(String message) {
        LogManager.getInstance().logFatal(message);
    }

    public static void addLogger(ILogger logger) {
        LogManager.getInstance().addLogger(logger);
    }

    public static void removeLogger(ILogger logger) {
        LogManager.getInstance().removeLogger(logger);
    }
}
