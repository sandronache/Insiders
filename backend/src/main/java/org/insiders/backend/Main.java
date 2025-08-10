package org.insiders.backend;

import org.insiders.backend.logger.FileLogger;
import org.insiders.backend.logger.LoggerFacade;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // Initialize logging BEFORE Spring Boot starts
        String logPath = "backend/src/main/resources/application.log";


        LoggerFacade.addLogger(new FileLogger(logPath));
        LoggerFacade.info("new version");
        LoggerFacade.info("Application starting up");

        SpringApplication.run(Main.class, args);

        LoggerFacade.info("Application shutting down");
        LoggerFacade.shutdown();
    }
}