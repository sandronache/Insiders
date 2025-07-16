package service;

import model.Comment;
import utils.Helper;
import logger.LoggerFacade;

import java.util.ArrayList;

public class CommentService {
    private final VotingService votingService;

    public CommentService(VotingService votingService) {
        this.votingService = votingService;
        LoggerFacade.debug("CommentService initialized");
    }

    public Comment createComment(String content, String username) {
        LoggerFacade.debug("Creating new comment by user: " + username);
        return new Comment(content, username, votingService.createVote());
    }

    public void addReply(Comment comment, String id, String content, String username) {
        ArrayList<Comment> replies = comment.getReplies();

        if (id.isEmpty()) {
            replies.add(createComment(content, username));
            LoggerFacade.info("Direct reply added to comment by user: " + username);
            return;
        }

        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= replies.size()) {
            LoggerFacade.warning("Failed to add reply with invalid ID: " + id);
            return;
        }

        String remaining_id = Helper.extractRemainingLevels(id);
        LoggerFacade.debug("Adding nested reply to comment path: " + id);
        addReply(replies.get(idx), remaining_id, content, username);
    }

    public void deleteComment(Comment comment, String id) {
        if (id.isEmpty()) {
            comment.setIsDeleted(true);
            LoggerFacade.info("Comment marked as deleted");
            return;
        }
        ArrayList<Comment> replies = comment.getReplies();

        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= replies.size()) {
            LoggerFacade.warning("Failed to delete comment with invalid ID: " + id);
            return;
        }

        String remaining_id = Helper.extractRemainingLevels(id);
        LoggerFacade.debug("Deleting nested comment at path: " + id);
        deleteComment(replies.get(idx), remaining_id);
    }

    public void addUpvote(Comment comment, String id, String username) {
        if (id.isEmpty()) {
            votingService.addUpvote(comment.getVote(), username);
            LoggerFacade.info("Upvote added to comment by user: " + username);
            return;
        }
        ArrayList<Comment> replies = comment.getReplies();

        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= replies.size()) {
            LoggerFacade.warning("Failed to upvote comment with invalid ID: " + id);
            return;
        }

        String remaining_id = Helper.extractRemainingLevels(id);
        LoggerFacade.debug("Adding upvote to nested comment at path: " + id);
        addUpvote(replies.get(idx), remaining_id, username);
    }

    public void addDownvote(Comment comment, String id, String username) {
        if (id.isEmpty()) {
            votingService.addDownvote(comment.getVote(), username);
            LoggerFacade.info("Downvote added to comment by user: " + username);
            return;
        }
        ArrayList<Comment> replies = comment.getReplies();

        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= replies.size()) {
            LoggerFacade.warning("Failed to downvote comment with invalid ID: " + id);
            return;
        }

        String remaining_id = Helper.extractRemainingLevels(id);
        LoggerFacade.debug("Adding downvote to nested comment at path: " + id);
        addDownvote(replies.get(idx), remaining_id, username);
    }

    public int getUpvoteCount(Comment comment) {
        return votingService.getUpvoteCount(comment.getVote());
    }

    public int getDownvoteCount(Comment comment) {
        return votingService.getDownvoteCount(comment.getVote());
    }

    public boolean isEmoji(Comment comment) {
        return votingService.isEmoji(comment.getVote());
    }

    // rendering function

    public void renderComment(Comment comment, StringBuilder sb, int depth, String id) {
        sb.append("     ".repeat(depth)).append("-> [").append(id).append("] ");
        sb.append('(').append(comment.getUsername()).append(')');
        if (isEmoji(comment))
            sb.append("🔥");
        sb.append("  ").append(comment.getContent()).append('\n');

        sb.append("   ".repeat(depth));
        sb.append("upvotes = ").append(getUpvoteCount(comment)).append("\n");

        sb.append("   ".repeat(depth));
        sb.append("downvotes = ").append(getDownvoteCount(comment)).append("\n\n\n");

        for (int i = 0; i < comment.getReplies().size(); i++) {
            renderComment(comment.getReplies().get(i), sb, depth + 1, id + '.' + i);
        }
    }
}
