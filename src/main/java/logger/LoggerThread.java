package main.java.logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LoggerThread extends Thread {
    private final BlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    public void log(String message) {
        boolean success = logQueue.offer(message);
        if (!success) {
            System.err.println("LoggerThread: Failed to enqueue log message: " + message);
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (running || !logQueue.isEmpty()) {
            try {
                String message = logQueue.take();
                // You can route this to ConsoleLogger, FileLogger, etc.
                System.out.println(message);
            } catch (InterruptedException e) {
                // Allow thread to exit if interrupted
            }
        }
    }
}
