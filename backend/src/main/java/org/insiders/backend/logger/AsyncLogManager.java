package org.insiders.backend.logger;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncLogManager {
    private static AsyncLogManager instance;
    private final List<ILogger> loggers = new CopyOnWriteArrayList<>();  //lista de logari(ConsoleLogger, FileLogger)
    private final BlockingQueue<LogEntry> logQueue = new LinkedBlockingQueue<>();  //coada unde sunt stocate logarile(BlockingQueue e Thread-Safe)
    private final Thread workerThread;  //thread pentru coada de loguri
    private volatile boolean running = true;   //flag pentru a putea opri threadul

    //creez un thread care va rula metoda processQueue
    private AsyncLogManager() {
        workerThread = new Thread(this::processQueue);
        workerThread.start();
    }

    public static synchronized AsyncLogManager getInstance() {
        if(instance == null){
            instance = new AsyncLogManager();
        }
        return instance;
    }

    //metoda pentru oprire fortata a loggerului
    public void shutdown() {
        running = false;
        workerThread.interrupt();  //trezeste threadul daca e blocat de take()
    }

    public void addLogger(ILogger logger) {
        loggers.add(logger);
    }

    public void removeLogger(ILogger logger) {
        loggers.remove(logger);
    }

    //creeaza o intrare(LogEntry) si o pune in coada
    public void log(String level, String message) {
        logQueue.offer(new LogEntry(level, message));
    }

    //metoda care ruleaza continuu cat timp flagul este setat la true sau coada nu este goala(ruleaza pe fundal)
    private void processQueue() {
        while (running || !logQueue.isEmpty()) {
            try {
                LogEntry entry = logQueue.take();  //asteapta un mesaj in coada. cand il detecteaza il preia. daca nu e niciun mesaj in coada, take() blocheaza threadul pana apare un log
                for (ILogger logger : loggers) {  //transmit mesajul catre toti logarii activi(ConsoleLogger, FileLogger)
                    switch (entry.level) {
                        case "DEBUG":  logger.logDebug(entry.message); break;
                        case "INFO":   logger.logInfo(entry.message); break;
                        case "WARNING":logger.logWarning(entry.message); break;
                        case "ERROR":  logger.logError(entry.message); break;
                        case "FATAL":  logger.logFatal(entry.message); break;
                    }

                }
            } catch (InterruptedException ignored) {}
        }
    }

    private static class LogEntry {
        final String level;
        final String message;

        LogEntry(String level, String message) {
            this.level = level;
            this.message = message;
        }
    }
}