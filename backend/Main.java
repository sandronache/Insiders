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

        FilesService filesService = FilesService.getInstance();
        VotingService votingService = VotingService.getInstance();
        CommentService commentService = CommentService.getInstance();
        ContentService contentService = ContentService.getInstance();
        AppDataService appDataService = AppDataService.getInstance();

        AppInterface app = new CLIInterface(contentService, appDataService);
        LoggerFacade.info("Starting application interface");
        app.run();
        LoggerFacade.info("Application shutting down");
    }
}