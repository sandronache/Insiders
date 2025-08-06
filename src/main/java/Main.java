package main.java;

// import main.java.app.AppInterface;
//  main.java.app.CLIInterface;

import main.java.logger.FileLogger;
import main.java.logger.ILogger;
import main.java.logger.LoggerFacade;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // Initialize logging BEFORE Spring Boot starts
        String logPath = "src/main/resources/application.log";


        LoggerFacade.addLogger(new FileLogger(logPath));
        LoggerFacade.info("new version");
        LoggerFacade.info("Application starting up");

        SpringApplication.run(Main.class, args);

        LoggerFacade.info("Application shutting down");
        LoggerFacade.shutdown();
    }
}