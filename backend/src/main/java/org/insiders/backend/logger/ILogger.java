package org.insiders.backend.logger;

public interface ILogger {

    void log(String message);

    void logDebug(String message);

    void logInfo(String message);

    void logWarning(String message);

    void logError(String message);

    void logFatal(String message);
}
