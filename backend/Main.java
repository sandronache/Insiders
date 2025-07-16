import app.AppInterface;
import app.CLIInterface;
import service.*;
import logger.*;

public class Main {
    public static void main(String[] args) {

        //ILogger consoleLogger = new ConsoleLogger();
        ILogger fileLogger = new FileLogger("application.log");
        //LoggerFacade.addLogger(consoleLogger);
        LoggerFacade.addLogger(fileLogger);

        LoggerFacade.info("Application starting up");

        VotingService votingService = new VotingService();
        CommentService commentService = new CommentService(votingService);
        ContentService contentService = new ContentService(votingService, commentService);
        AppDataService appDataService = new AppDataService(contentService);

        AppInterface app = new CLIInterface(contentService, appDataService);
        LoggerFacade.info("Starting application interface");
        app.run();
        LoggerFacade.info("Application shutting down");
    }
}