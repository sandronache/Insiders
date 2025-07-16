package service;

import logger.LoggerFacade;
import model.Comment;
import util.Helper;

import java.util.TreeMap;

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

    public boolean addReply(Comment comment, String id, String content, String username) {
        TreeMap<Integer, Comment> replies = comment.getReplies();
        if (id.isEmpty()) {
            if (comment.isDeleted()) {
                LoggerFacade.warning("Cannot add reply to a deleted comment by user: " + username);
                return false;
            }

            Comment reply = createComment(content, username);

            Integer newId = comment.getIdNextReply();
            comment.setIdNextReply(newId + 1);

            replies.put(newId, reply);

            LoggerFacade.info("Direct reply added to comment by user: " + username);
            return true;
        }

        int idx = Helper.extractFirstLevel(id);
        if (!replies.containsKey(idx)) {
            LoggerFacade.warning("Failed to add reply with invalid ID: " + id);
            return false;
        }
        LoggerFacade.debug("Adding nested reply to comment path: " + id);

        String remaining_id = Helper.extractRemainingLevels(id);
        return addReply(replies.get(idx), remaining_id, content, username);
    }

    public void deleteComment(Comment comment, String id) {
        if (id.isEmpty()) {
            comment.setIsDeleted(true);

            LoggerFacade.info("Comment marked as deleted");
            return;
        }
        TreeMap<Integer,Comment> replies = comment.getReplies();

        int idx = Helper.extractFirstLevel(id);
        if (!replies.containsKey(idx)) {
            LoggerFacade.warning("Failed to delete comment with invalid ID: " + id);
            return;
        }
        LoggerFacade.debug("Deleting nested comment at path: " + id);

        String remaining_id = Helper.extractRemainingLevels(id);
        deleteComment(replies.get(idx), remaining_id);
    }

    public void addUpvote(Comment comment, String id, String username) {
        if (id.isEmpty()) {
            if (comment.isDeleted()) {
                LoggerFacade.warning("Cannot upvote a deleted comment by user: " + username);
                return;
            }

            votingService.addUpvote(comment.getVote(), username);

            LoggerFacade.info("Upvote added to comment by user: " + username);
            return;
        }
        TreeMap<Integer, Comment> replies = comment.getReplies();

        int idx = Helper.extractFirstLevel(id);
        if (!replies.containsKey(idx)) {
            LoggerFacade.warning("Failed to upvote comment with invalid ID: " + id);
            return;
        }
        LoggerFacade.debug("Adding upvote to nested comment at path: " + id);

        String remaining_id = Helper.extractRemainingLevels(id);
        addUpvote(replies.get(idx), remaining_id, username);
    }

    public void addDownvote(Comment comment, String id, String username) {
        if (id.isEmpty()) {
            if (comment.isDeleted()) {
                LoggerFacade.warning("Cannot downvote a deleted comment by user: " + username);
                return;
            }

            votingService.addDownvote(comment.getVote(), username);

            LoggerFacade.info("Downvote added to comment by user: " + username);
            return;
        }
        TreeMap<Integer, Comment> replies = comment.getReplies();

        int idx = Helper.extractFirstLevel(id);
        if (!replies.containsKey(idx)) {
            LoggerFacade.warning("Failed to downvote comment with invalid ID: " + id);
            return;
        }
        LoggerFacade.debug("Adding downvote to nested comment at path: " + id);

        String remaining_id = Helper.extractRemainingLevels(id);
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

        comment.getReplies().forEach((idReply, reply) ->
                renderComment(reply, sb, depth + 1, id + '.' + idReply));
    }
}
