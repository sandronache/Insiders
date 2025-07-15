import app.AppInterface;
import app.CLIInterface;
import service.*;

public class Main {
    public static void main(String[] args) {
        VotingService votingService = new VotingService();
        CommentService commentService = new CommentService(votingService);
        ContentService contentService = new ContentService(votingService, commentService);
        AppDataService appDataService = new AppDataService(contentService);

        AppInterface app = new CLIInterface(contentService, appDataService);
        app.run();
    }
}