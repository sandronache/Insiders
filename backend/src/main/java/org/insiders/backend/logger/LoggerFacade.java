package org.insiders.backend.logger;

public class LoggerFacade {
    private static final AsyncLogManager manager = AsyncLogManager.getInstance();

    public static void debug(String message) {
        manager.log("DEBUG",message);
    }

    public static void info(String message) {
        manager.log("INFO",message);
    }

    public static void warning(String message) {
        manager.log("WARNING",message);
    }

    public static void error(String message) {
        manager.log("ERROR",message);
    }

    public static void fatal(String message) {
        manager.log("FATAL",message);
    }

    public static void addLogger(ILogger logger) {
        manager.addLogger(logger);
    }

    public static void removeLogger(ILogger logger) {
        manager.removeLogger(logger);
    }

    public static void shutdown(){
        manager.shutdown();
    }
}
