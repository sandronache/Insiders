package main.java;

import main.java.app.AppInterface;
import main.java.app.CLIInterface;
import main.java.logger.FileLogger;
import main.java.logger.ILogger;
import main.java.logger.LoggerFacade;
import main.java.service.*;

import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        final String loggerLocation = Objects.isNull(System.getenv("IS_DEV"))? "/mnt/resources/application.log" : "resources/application.log";

        //ILogger consoleLogger = new ConsoleLogger();
        ILogger fileLogger = new FileLogger(loggerLocation);

        //LoggerFacade.addLogger(consoleLogger);
        LoggerFacade.addLogger(fileLogger);
        LoggerFacade.info("new version");
        LoggerFacade.info("Application starting up");

        // Initialize database instead of FilesService
        DatabaseInitService databaseInitService = DatabaseInitService.getInstance();

        // Test database connection and initialize schema
        if (!databaseInitService.testConnection()) {
            LoggerFacade.fatal("Cannot connect to database. Application shutting down.");
            System.exit(1);
        }

        try {
            databaseInitService.initializeDatabase();
            LoggerFacade.info("Database initialized successfully");
        } catch (Exception e) {
            LoggerFacade.fatal("Database initialization failed: " + e.getMessage());
            System.exit(1);
        }

        // Initialize services - they now use repositories instead of files
        VotingService.getInstance();
        CommentService.getInstance();
        ContentService contentService = ContentService.getInstance();
        AppDataService appDataService = AppDataService.getInstance();

        AppInterface app = new CLIInterface(contentService, appDataService);
        LoggerFacade.info("Starting application interface");
        app.run();
        LoggerFacade.info("Application shutting down");
    }
}