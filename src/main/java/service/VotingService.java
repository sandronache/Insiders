package main.java.service;

import main.java.logger.LoggerFacade;
import main.java.model.Vote;
import main.java.repository.VoteRepository;

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

    // New method to check emoji and sync with database for posts
    public void checkEmojiForPost(Vote vote, Integer postId) {
        int upvotesSize = getUpvoteCount(vote);
        int downvotesSize = getDownvoteCount(vote);
        boolean wasEmoji = vote.isEmoji();
        boolean shouldBeEmoji = upvotesSize - downvotesSize >= 10;

        vote.setEmoji(shouldBeEmoji);

        // Save to database if we have a post ID
        if (postId != null) {
            try {
                VoteRepository voteRepository = new VoteRepository();
                voteRepository.setPostEmojiFlag(postId, shouldBeEmoji);
            } catch (Exception e) {
                LoggerFacade.warning("Could not save post emoji flag to database: " + e.getMessage());
            }
        }

        if (!wasEmoji && vote.isEmoji()) {
            LoggerFacade.info("Post achieved emoji status with score: " + (upvotesSize - downvotesSize));
        } else if (wasEmoji && !vote.isEmoji()) {
            LoggerFacade.info("Post lost emoji status with score: " + (upvotesSize - downvotesSize));
        }
    }

    // New method to check emoji and sync with database for comments
    public void checkEmojiForComment(Vote vote, Integer commentId) {
        int upvotesSize = getUpvoteCount(vote);
        int downvotesSize = getDownvoteCount(vote);
        boolean wasEmoji = vote.isEmoji();
        boolean shouldBeEmoji = upvotesSize - downvotesSize >= 10;

        vote.setEmoji(shouldBeEmoji);

        // Save to database if we have a comment ID
        if (commentId != null) {
            try {
                VoteRepository voteRepository = new VoteRepository();
                voteRepository.setCommentEmojiFlag(commentId, shouldBeEmoji);
            } catch (Exception e) {
                LoggerFacade.warning("Could not save comment emoji flag to database: " + e.getMessage());
            }
        }

        if (!wasEmoji && vote.isEmoji()) {
            LoggerFacade.info("Comment achieved emoji status with score: " + (upvotesSize - downvotesSize));
        } else if (wasEmoji && !vote.isEmoji()) {
            LoggerFacade.info("Comment lost emoji status with score: " + (upvotesSize - downvotesSize));
        }
    }

    // Load emoji status from database into memory
    public void loadEmojiFromDatabase(Vote vote, Integer postId, Integer commentId) {
        try {
            VoteRepository voteRepository = new VoteRepository();
            boolean databaseEmoji = false;

            if (postId != null) {
                databaseEmoji = voteRepository.getPostEmojiFlag(postId);
            } else if (commentId != null) {
                databaseEmoji = voteRepository.getCommentEmojiFlag(commentId);
            }

            vote.setEmoji(databaseEmoji);
            LoggerFacade.debug("Loaded emoji status from database: " + databaseEmoji);

        } catch (Exception e) {
            LoggerFacade.warning("Could not load emoji status from database: " + e.getMessage());
            // Fallback to calculation based on votes
            checkEmoji(vote);
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
