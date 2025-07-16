package service;

import logger.LoggerFacade;
import model.Comment;
import model.Post;
import util.Helper;

import java.util.TreeMap;

public class ContentService {
    private final VotingService votingService;
    private final CommentService commentService;

    public ContentService(VotingService votingService, CommentService commentService) {
        this.votingService = votingService;
        this.commentService = commentService;

        LoggerFacade.debug("ContentService initialized");
    }

    public Post createPost(String content, String username) {
        LoggerFacade.debug("Creating new post for user: " + username);

        return new Post(content, username, votingService.createVote());
    }

    public void addComment(Post post, String content, String username) {
        Comment comment = commentService.createComment(content, username);

        Integer id = post.getIdNextComment();
        post.setIdNextComment(id + 1);

        post.getComments().put(id, comment);

        LoggerFacade.info("Comment added to post by user: " + username);
    }
    public void deleteCommentOrReply(Post post, String id) {
        TreeMap<Integer, Comment> comments = post.getComments();

        int idx = Helper.extractFirstLevel(id);
        if (!comments.containsKey(idx)) {
            LoggerFacade.warning("Failed to delete comment with invalid ID: " + id);
            return;
        }
        LoggerFacade.info("Deleting comment/reply with ID: " + id);

        String remaining_id = Helper.extractRemainingLevels(id);
        commentService.deleteComment(comments.get(idx), remaining_id);
    }

    public boolean addReply(Post post, String id, String content, String username) {
        TreeMap<Integer, Comment> comments = post.getComments();

        int idx = Helper.extractFirstLevel(id);
        if (!comments.containsKey(idx)) {
            LoggerFacade.warning("Failed to add reply with invalid comment ID: " + id);
            return false;
        }
        LoggerFacade.info("Adding reply to comment ID: " + id + " by user: " + username);

        String remaining_id = Helper.extractRemainingLevels(id);
        return commentService.addReply(comments.get(idx), remaining_id, content, username);
    }

    public void addUpvoteComment(Post post, String id, String username) {
        TreeMap<Integer, Comment> comments = post.getComments();

        int idx = Helper.extractFirstLevel(id);
        if (!comments.containsKey(idx)) {
            LoggerFacade.warning("Failed to add upvote with invalid comment ID: " + id);
            return;
        }
        LoggerFacade.info("Adding upvote to comment ID: " + id + " by user: " + username);

        String remaining_id = Helper.extractRemainingLevels(id);
        commentService.addUpvote(comments.get(idx), remaining_id, username);
    }

    public void addDownvoteComment(Post post, String id, String username) {
        TreeMap<Integer, Comment> comments = post.getComments();

        int idx = Helper.extractFirstLevel(id);
        if (!comments.containsKey(idx)) {
            LoggerFacade.warning("Failed to add downvote with invalid comment ID: " + id);
            return;
        }
        LoggerFacade.info("Adding downvote to comment ID: " + id + " by user: " + username);

        String remaining_id = Helper.extractRemainingLevels(id);
        commentService.addDownvote(comments.get(idx), remaining_id, username);
    }

    public void addUpvotePost(Post post, String username) {
        votingService.addUpvote(post.getVote(), username);
    }
    public void addDownvotePost(Post post, String username) {
        votingService.addDownvote(post.getVote(), username);
    }

    public int getUpvoteCount(Post post) {
        return votingService.getUpvoteCount(post.getVote());
    }
    public int getDownvoteCount(Post post) {
        return votingService.getDownvoteCount(post.getVote());
    }

    public boolean isEmoji(Post post) {
        return votingService.isEmoji(post.getVote());
    }

    // rendering functions

    public String renderFeedPost(Post post, String id) {
        StringBuilder sb = new StringBuilder();

        sb.append("post ").append(id).append(" by (").append(post.getUsername()).append("):\n\n");
        if (isEmoji(post)) {
            sb.append("🔥 ");
        }
        sb.append(post.getContent()).append("\n\n");
        sb.append("upvotes = ").append(getUpvoteCount(post)).append("\n");
        sb.append("downvotes = ").append(getDownvoteCount(post)).append("\n\n");
        return sb.toString();
    }

    public String renderFullPost(Post post) {
        StringBuilder sb = new StringBuilder();

        sb.append('(').append(post.getUsername()).append("):\n");
        if (isEmoji(post)) {
            sb.append("🔥 ");
        }
        sb.append(post.getContent()).append("\n\n");
        sb.append("upvotes = ").append(getUpvoteCount(post)).append("\n");
        sb.append("downvotes = ").append(getDownvoteCount(post)).append("\n\n\n");

        post.getComments().forEach((id, comment) ->
                commentService.renderComment(comment, sb, 1, id.toString())
        );

        return sb.toString();
    }
}
