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

        FilesService.getInstance();
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