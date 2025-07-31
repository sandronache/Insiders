package main.java;

// import main.java.app.AppInterface;
//  main.java.app.CLIInterface;
import main.java.logger.FileLogger;
import main.java.logger.ILogger;
import main.java.logger.LoggerFacade;
import main.java.service.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

import java.util.Objects;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // Initialize logging BEFORE Spring Boot starts
        final String loggerLocation = Objects.isNull(System.getenv("IS_DEV")) ?
            "src/main/resources/application.log" : "src/main/resources/application.log";

        //ILogger consoleLogger = new ConsoleLogger();
        ILogger fileLogger = new FileLogger(loggerLocation);

        //LoggerFacade.addLogger(consoleLogger);
        LoggerFacade.addLogger(fileLogger);
        LoggerFacade.info("new version");
        LoggerFacade.info("Application starting up");

        // Now start Spring Boot
        SpringApplication.run(Main.class, args);

        // Initialize database instead of FilesService
        //DatabaseInitService databaseInitService = DatabaseInitService.getInstance();

//        // Test database connection and initialize schema
//        if (!databaseInitService.testConnection()) {
//            LoggerFacade.fatal("Cannot connect to database. Application shutting down.");
//            System.exit(1);
//        }
//
//        try {
//            databaseInitService.initializeDatabase();
//            LoggerFacade.info("Database initialized successfully");
//        } catch (Exception e) {
//            LoggerFacade.fatal("Database initialization failed: " + e.getMessage());
//            System.exit(1);
//        }

        // Initialize services - they now use repositories instead of files
        VotingService.getInstance();
        CommentService.getInstance();
        ContentService contentService = ContentService.getInstance();

        // AppInterface app = new CLIInterface(contentService, appDataService);
        // LoggerFacade.info("Starting application interface");
        // app.run();
        // LoggerFacade.info("Application shutting down");

        LoggerFacade.shutdown();
    }
}