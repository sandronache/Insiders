import app.AppInterface;
import app.CLIInterface;
import logger.FileLogger;
import logger.ILogger;
import logger.LoggerFacade;
import service.*;

public class Main {
    public static void main(String[] args) {

        //ILogger consoleLogger = new ConsoleLogger();
        ILogger fileLogger = new FileLogger("application.log");

        //LoggerFacade.addLogger(consoleLogger);
        LoggerFacade.addLogger(fileLogger);

        LoggerFacade.info("Application starting up");

        FilesService filesService = new FilesService();
        VotingService votingService = new VotingService();
        CommentService commentService = new CommentService(votingService);
        ContentService contentService = new ContentService(votingService, commentService);
        AppDataService appDataService = new AppDataService(filesService, contentService);

        AppInterface app = new CLIInterface(contentService, appDataService);
        LoggerFacade.info("Starting application interface");
        app.run();
        LoggerFacade.info("Application shutting down");
    }
}