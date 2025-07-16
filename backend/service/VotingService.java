package service;

import logger.LoggerFacade;
import model.Vote;

import java.util.Set;

public class VotingService {
    private static VotingService instance;

    private VotingService() {}

    public static VotingService getInstance() {
        if (instance == null) {
            instance = new VotingService();
        }
        return instance;
    }

    public Vote createVote() {
        LoggerFacade.debug("New vote object created");
        return new Vote();
    }

    public int getUpvoteCount(Vote vote) {
        return vote.getUpvote().size();
    }
    public int getDownvoteCount(Vote vote) {
        return vote.getDownvote().size();
    }

    public void checkEmoji(Vote vote) {
        int upvotesSize = getUpvoteCount(vote);
        int downvotesSize = getDownvoteCount(vote);
        boolean wasEmoji = vote.isEmoji();
        vote.setEmoji(upvotesSize - downvotesSize >= 10);

        if (!wasEmoji && vote.isEmoji()) {
            LoggerFacade.info("Content achieved emoji status with score: " + (upvotesSize - downvotesSize));
        } else if (wasEmoji && !vote.isEmoji()) {
            LoggerFacade.info("Content lost emoji status with score: " + (upvotesSize - downvotesSize));
        }
    }

    private void toggleVote(Vote vote, Set<String> first, Set<String> second, String username) {
        if (first.contains(username)) {
            first.remove(username);
            LoggerFacade.info("User " + username + " removed vote");
        } else {
            second.remove(username);
            first.add(username);
            LoggerFacade.info("User " + username + " added or changed vote");
        }
        checkEmoji(vote);
    }

    public void addUpvote(Vote vote, String username) {
        LoggerFacade.debug("User " + username + " is upvoting content");
        toggleVote(vote, vote.getUpvote(), vote.getDownvote(), username);
    }

    public void addDownvote(Vote vote, String username) {
        LoggerFacade.debug("User " + username + " is downvoting content");
        toggleVote(vote, vote.getDownvote(), vote.getUpvote(), username);
    }

    public boolean isEmoji(Vote vote) {
        return vote.isEmoji();
    }
}
