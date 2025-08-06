package main.java.service;

import main.java.entity.Comment;
import main.java.entity.Post;
import main.java.entity.User;
import main.java.entity.Vote;
import main.java.exceptions.NotFoundException;
import main.java.logger.LoggerFacade;
import main.java.repository.CommentRepository;
import main.java.repository.VoteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class VotingService {

    private final VoteRepository voteRepository;
    private final PostManagementService postManagementService;
    private final UserManagementService userManagementService;
    private final CommentService commentService;
    private final CommentRepository commentRepository;

    public VotingService(VoteRepository voteRepository, PostManagementService postManagementService, UserManagementService userManagementService, CommentService commentService, CommentRepository commentRepository) {
        this.voteRepository = voteRepository;
        this.postManagementService = postManagementService;
        this.userManagementService = userManagementService;
        this.commentService = commentService;
        this.commentRepository = commentRepository;
    }

    //de pastrat la refactor
    public void createVote(UUID userId, UUID postId, UUID commentId, boolean isUpvote) {
        User user = userManagementService.findById(userId);

        if (postId != null && commentId != null) {
            throw new IllegalArgumentException("Un vot nu poate fi aplicat simultan la o postare si un comentariu.");
        }

        if (postId != null) {
            Optional<Vote> existingVote = voteRepository.findByUserIdAndPostId(userId, postId);
            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();
                vote.setUpvote(isUpvote);
                voteRepository.save(vote);
            } else {
                Post post = postManagementService.getPostById(postId);
                Vote vote = new Vote(post, null, user, isUpvote);
                voteRepository.save(vote);
            }
        } else if (commentId != null) {
            Optional<Vote> existingVote = voteRepository.findByUserIdAndCommentId(userId, commentId);
            if (existingVote.isPresent()) {
                Vote vote = existingVote.get();
                vote.setUpvote(isUpvote);
                voteRepository.save(vote);
            } else {
                Comment comment = commentService.findById(commentId);
                Vote vote = new Vote(null, comment, user, isUpvote);
                voteRepository.save(vote);
            }
        } else {
            throw new IllegalArgumentException("Trebuie sa specifici fie postId, fie commentId.");
        }
    }


    //de pastrat la refactor
    public void deleteVoteForComment(Comment comment, User user) {
        voteRepository.deleteByUserIdAndCommentId(user.getId(), comment.getId());
    }

    //de pastrat la refactor
    public int countUpvotesForComment(UUID commentId) {
        return voteRepository.countByCommentIdAndUpvoteTrue(commentId);
    }

    //de pastrat la refactor
    public int countDownvotesForComment(UUID commentId) {
        return voteRepository.countByCommentIdAndUpvoteFalse(commentId);
    }

    //de pastrat la refactor
    public String getVoteTypeForUser(UUID userId, UUID postId, UUID commentId) {
        if (postId != null) {
            return voteRepository.findByUserIdAndPostId(userId, postId)
                    .map(v -> v.isUpvote() ? "up" : "down")
                    .orElse("none");
        } else if (commentId != null) {
            return voteRepository.findByUserIdAndCommentId(userId, commentId)
                    .map(v -> v.isUpvote() ? "up" : "down")
                    .orElse("none");
        }
        throw new IllegalArgumentException("Trebuie să specifici postId sau commentId");
    }

    //de pastrat la refactor
    public int countUpvotesForPost(UUID postId) {
        return voteRepository.countByPostIdAndUpvoteTrue(postId);
    }

    //de pastrat la refactor
    public int countDownvotesForPost(UUID postId) {
        return voteRepository.countByPostIdAndUpvoteFalse(postId);
    }

    //de pastrat la refactor
    public void deleteVoteForPost(Post post, User user){
        voteRepository.deleteByUserIdAndPostId(user.getId(), post.getId());
    }

    public UUID getUserIdByUsername(String username){
        return userManagementService.findByUsername(username).getId();
    }



//    public Vote createVote() {
//        LoggerFacade.debug("New vote object created");
//        return new Vote();
//    }
//
//    public int getUpvoteCount(Vote vote) {
//        return vote.getUpvote().size();
//    }
//    public int getDownvoteCount(Vote vote) {
//        return vote.getDownvote().size();
//    }
//
//    public void checkEmoji(Vote vote) {
//        int upvotesSize = getUpvoteCount(vote);
//        int downvotesSize = getDownvoteCount(vote);
//        boolean wasEmoji = vote.isEmoji();
//        vote.setEmoji(upvotesSize - downvotesSize >= 10);
//
//        if (!wasEmoji && vote.isEmoji()) {
//            LoggerFacade.info("Content achieved emoji status with score: " + (upvotesSize - downvotesSize));
//        } else if (wasEmoji && !vote.isEmoji()) {
//            LoggerFacade.info("Content lost emoji status with score: " + (upvotesSize - downvotesSize));
//        }
//    }
//
//    // New method to check emoji and sync with database for posts
//    public void checkEmojiForPost(Vote vote, UUID postId) {
//        int upvotesSize = getUpvoteCount(vote);
//        int downvotesSize = getDownvoteCount(vote);
//        boolean wasEmoji = vote.isEmoji();
//        boolean shouldBeEmoji = upvotesSize - downvotesSize >= 10;
//
//        vote.setEmoji(shouldBeEmoji);
//
//        // Save to database if we have a post ID
//        if (postId != null) {
//            try {
//                VoteRepository voteRepository = new VoteRepository();
//                voteRepository.setPostEmojiFlag(postId, shouldBeEmoji);
//            } catch (Exception e) {
//                LoggerFacade.warning("Could not save post emoji flag to database: " + e.getMessage());
//            }
//        }
//
//        if (!wasEmoji && vote.isEmoji()) {
//            LoggerFacade.info("Post achieved emoji status with score: " + (upvotesSize - downvotesSize));
//        } else if (wasEmoji && !vote.isEmoji()) {
//            LoggerFacade.info("Post lost emoji status with score: " + (upvotesSize - downvotesSize));
//        }
//    }
//
//    // New method to check emoji and sync with database for comments
//    public void checkEmojiForComment(Vote vote, Integer commentId) {
//        int upvotesSize = getUpvoteCount(vote);
//        int downvotesSize = getDownvoteCount(vote);
//        boolean wasEmoji = vote.isEmoji();
//        boolean shouldBeEmoji = upvotesSize - downvotesSize >= 10;
//
//        vote.setEmoji(shouldBeEmoji);
//
//        // Save to database if we have a comment ID
//        if (commentId != null) {
//            try {
//                VoteRepository voteRepository = new VoteRepository();
//                voteRepository.setCommentEmojiFlag(commentId, shouldBeEmoji);
//            } catch (Exception e) {
//                LoggerFacade.warning("Could not save comment emoji flag to database: " + e.getMessage());
//            }
//        }
//
//        if (!wasEmoji && vote.isEmoji()) {
//            LoggerFacade.info("Comment achieved emoji status with score: " + (upvotesSize - downvotesSize));
//        } else if (wasEmoji && !vote.isEmoji()) {
//            LoggerFacade.info("Comment lost emoji status with score: " + (upvotesSize - downvotesSize));
//        }
//    }
//
//    // Load emoji status from database into memory
//    public void loadEmojiFromDatabase(Vote vote, UUID postId, Integer commentId) {
//        try {
//            VoteRepository voteRepository = new VoteRepository();
//            boolean databaseEmoji = false;
//
//            if (postId != null) {
//                databaseEmoji = voteRepository.getPostEmojiFlag(postId);
//            } else if (commentId != null) {
//                databaseEmoji = voteRepository.getCommentEmojiFlag(commentId);
//            }
//
//            vote.setEmoji(databaseEmoji);
//            LoggerFacade.debug("Loaded emoji status from database: " + databaseEmoji);
//
//        } catch (Exception e) {
//            LoggerFacade.warning("Could not load emoji status from database: " + e.getMessage());
//            // Fallback to calculation based on votes
//            checkEmoji(vote);
//        }
//    }
//
//    private void toggleVote(Vote vote, Set<String> first, Set<String> second, String username) {
//        if (first.contains(username)) {
//            first.remove(username);
//            LoggerFacade.info("User " + username + " removed vote");
//        } else {
//            second.remove(username);
//            first.add(username);
//            LoggerFacade.info("User " + username + " added or changed vote");
//        }
//        checkEmoji(vote);
//    }
//
//    public void addUpvote(Vote vote, String username) {
//        LoggerFacade.debug("User " + username + " is upvoting content");
//        toggleVote(vote, vote.getUpvote(), vote.getDownvote(), username);
//    }
//
//    public void addDownvote(Vote vote, String username) {
//        LoggerFacade.debug("User " + username + " is downvoting content");
//        toggleVote(vote, vote.getDownvote(), vote.getUpvote(), username);
//    }
//
//    public boolean isEmoji(Vote vote) {
//        return vote.isEmoji();
//    }
}
