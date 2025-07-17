package main.java;

import main.java.app.AppInterface;
import main.java.app.CLIInterface;
import main.java.logger.FileLogger;
import main.java.logger.ILogger;
import main.java.logger.LoggerFacade;
import main.java.service.*;

public class Main {
    public static void main(String[] args) {

        //ILogger consoleLogger = new ConsoleLogger();
        ILogger fileLogger = new FileLogger("application.log");

        //LoggerFacade.addLogger(consoleLogger);
        LoggerFacade.addLogger(fileLogger);

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