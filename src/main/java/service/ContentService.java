package main.java.service;

import main.java.logger.LoggerFacade;
import main.java.model.Comment;
import main.java.model.Post;
import main.java.repository.CommentRepository;
import main.java.repository.PostRepository;
import main.java.repository.VoteRepository;
import main.java.util.Helper;

import java.util.TreeMap;
import java.util.Optional;

public class ContentService {
    private static ContentService instance;
    private final VotingService votingService;
    private final CommentService commentService;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    private ContentService(VotingService votingService,
                            CommentService commentService) {
        this.votingService = votingService;
        this.commentService = commentService;
        this.commentRepository = new CommentRepository();
        this.postRepository = new PostRepository();
    }

    public static ContentService getInstance() {
        if (instance == null) {
            instance = new ContentService(VotingService.getInstance(),
                                            CommentService.getInstance());
        }
        return instance;
    }

    public Post createPost(String content, String username) {
        LoggerFacade.debug("Creating new post for user: " + username);

        return new Post(content, username, votingService.createVote());
    }

    public void addComment(Post post, String content, String username) {
        LoggerFacade.debug("Adding comment to post by user: " + username);

        // Find the post ID from database by content (since Post model doesn't have ID)
        Optional<Post> dbPost = postRepository.findByContent(post.getContent());
        if (!dbPost.isPresent()) {
            LoggerFacade.warning("Post not found in database, cannot add comment");
            return;
        }

        // Create the comment
        Comment comment = commentService.createComment(content, username);

        // Get the post ID from database - we need to add a method to get the ID
        // For now, let's assume we can get it somehow. In a real scenario, Post should have an ID field
        Integer postId = findPostIdByContent(post.getContent());
        if (postId == null) {
            LoggerFacade.warning("Could not determine post ID for adding comment");
            return;
        }

        // Save comment to database (null for parentCommentId since this is a top-level comment)
        Integer commentId = commentRepository.save(comment, postId, null);

        if (commentId != null) {
            // Also update the in-memory structure for consistency
            Integer id = post.getIdNextComment();
            post.setIdNextComment(id + 1);
            post.getComments().put(id, comment);

            LoggerFacade.info("Comment added successfully with ID: " + commentId);
        } else {
            LoggerFacade.error("Failed to save comment to database");
        }
    }

    private Integer findPostIdByContent(String content) {
        // This is a helper method to find post ID by content
        // In a real application, Post should have an ID field
        String sql = "SELECT id FROM posts WHERE content = ?";

        try (java.sql.Connection conn = main.java.util.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, content);
            java.sql.ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (java.sql.SQLException e) {
            LoggerFacade.fatal("Error finding post ID: " + e.getMessage());
        }

        return null;
    }

    public void deleteCommentOrReply(Post post, String id) {
        LoggerFacade.info("Deleting comment/reply with ID: " + id);

        // Get post ID from database
        Integer postId = findPostIdByContent(post.getContent());
        if (postId == null) {
            LoggerFacade.warning("Could not determine post ID for deleting comment");
            return;
        }

        int idx = Helper.extractFirstLevel(id);
        String remaining_id = Helper.extractRemainingLevels(id);

        // For top-level comments
        if (remaining_id.isEmpty()) {
            Integer dbCommentId = findCommentIdByIndex(postId, idx);
            if (dbCommentId != null) {
                // Mark as deleted in database
                commentRepository.markAsDeleted(dbCommentId);

                // Also update in-memory structure
                TreeMap<Integer, Comment> comments = post.getComments();
                if (comments.containsKey(idx)) {
                    commentService.deleteComment(comments.get(idx), "");
                }

                LoggerFacade.info("Comment marked as deleted in database: " + dbCommentId);
            }
        } else {
            // For nested replies, fall back to in-memory for now
            TreeMap<Integer, Comment> comments = post.getComments();
            if (comments.containsKey(idx)) {
                commentService.deleteComment(comments.get(idx), remaining_id);
            }
        }
    }

    public boolean addReply(Post post, String id, String content, String username) {
        LoggerFacade.info("Adding reply to comment ID: " + id + " by user: " + username);

        // Get post ID from database
        Integer postId = findPostIdByContent(post.getContent());
        if (postId == null) {
            LoggerFacade.warning("Could not determine post ID for adding reply");
            return false;
        }

        // Parse the comment ID to get the parent comment ID
        int parentCommentIdx = Helper.extractFirstLevel(id);
        String remainingId = Helper.extractRemainingLevels(id);

        // For now, let's handle direct replies to top-level comments
        // In a more complex system, you'd need to handle nested replies
        if (!remainingId.isEmpty()) {
            LoggerFacade.debug("Handling nested reply with database persistence");

            // Find the parent comment in database
            Integer dbCommentId = findCommentIdByIndex(postId, parentCommentIdx);
            if (dbCommentId == null) {
                LoggerFacade.warning("Could not find parent comment in database for nested reply");
                return false;
            }

            // Find the nested parent comment by traversing the path
            Integer nestedParentId = findNestedCommentId(dbCommentId, remainingId);
            if (nestedParentId == null) {
                LoggerFacade.warning("Could not find nested parent comment in database");
                return false;
            }

            // Create the reply comment
            Comment reply = commentService.createComment(content, username);

            // Save nested reply to database
            Integer replyId = commentRepository.save(reply, postId, nestedParentId);

            if (replyId != null) {
                // Also update in-memory structure for consistency
                TreeMap<Integer, Comment> comments = post.getComments();
                if (comments.containsKey(parentCommentIdx)) {
                    commentService.addReply(comments.get(parentCommentIdx), remainingId, content, username);
                }

                LoggerFacade.info("Nested reply added successfully with database ID: " + replyId);
                return true;
            } else {
                LoggerFacade.error("Failed to save nested reply to database");
                return false;
            }
        }

        // For direct replies to top-level comments, we need to find the database comment ID
        // This is a simplification - in reality you'd need a better mapping system
        Integer dbCommentId = findCommentIdByIndex(postId, parentCommentIdx);
        if (dbCommentId == null) {
            LoggerFacade.warning("Could not find parent comment in database");
            return false;
        }

        // Create the reply comment
        Comment reply = commentService.createComment(content, username);

        // Save reply to database with parent comment ID
        Integer replyId = commentRepository.save(reply, postId, dbCommentId);

        if (replyId != null) {
            // Also update in-memory structure for consistency
            TreeMap<Integer, Comment> comments = post.getComments();
            if (comments.containsKey(parentCommentIdx)) {
                Comment parentComment = comments.get(parentCommentIdx);
                Integer nextReplyId = parentComment.getIdNextReply();
                parentComment.setIdNextReply(nextReplyId + 1);
                parentComment.getReplies().put(nextReplyId, reply);
            }

            LoggerFacade.info("Reply added successfully with database ID: " + replyId);
            return true;
        } else {
            LoggerFacade.error("Failed to save reply to database");
            return false;
        }
    }

    private Integer findCommentIdByIndex(Integer postId, Integer commentIndex) {
        // Helper method to find the database comment ID by post ID and comment index
        // This is a workaround since we're using TreeMap indices in memory
        String sql = "SELECT id FROM comments WHERE post_id = ? AND parent_comment_id IS NULL ORDER BY created_at LIMIT 1 OFFSET ?";

        try (java.sql.Connection conn = main.java.util.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            stmt.setInt(2, commentIndex);
            java.sql.ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (java.sql.SQLException e) {
            LoggerFacade.fatal("Error finding comment ID by index: " + e.getMessage());
        }

        return null;
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
        // Add upvote in memory
        votingService.addUpvote(post.getVote(), username);

        // Also save to database
        Integer postId = findPostIdByContent(post.getContent());
        if (postId != null) {
            VoteRepository voteRepository = new VoteRepository();
            voteRepository.addPostUpvote(postId, username);
            LoggerFacade.info("Post upvote saved to database for post ID: " + postId + " by user: " + username);
        } else {
            LoggerFacade.warning("Could not find post ID to save upvote to database");
        }
    }

    public void addDownvotePost(Post post, String username) {
        // Add downvote in memory
        votingService.addDownvote(post.getVote(), username);

        // Also save to database
        Integer postId = findPostIdByContent(post.getContent());
        if (postId != null) {
            VoteRepository voteRepository = new VoteRepository();
            voteRepository.addPostDownvote(postId, username);
            LoggerFacade.info("Post downvote saved to database for post ID: " + postId + " by user: " + username);
        } else {
            LoggerFacade.warning("Could not find post ID to save downvote to database");
        }
    }

    public int getUpvoteCount(Post post) {
        // Get from database instead of just memory
        Integer postId = findPostIdByContent(post.getContent());
        if (postId != null) {
            VoteRepository voteRepository = new VoteRepository();
            return voteRepository.getPostUpvoteCount(postId);
        }
        // Fallback to memory if post ID not found
        return votingService.getUpvoteCount(post.getVote());
    }
    public int getDownvoteCount(Post post) {
        // Get from database instead of just memory
        Integer postId = findPostIdByContent(post.getContent());
        if (postId != null) {
            VoteRepository voteRepository = new VoteRepository();
            return voteRepository.getPostDownvoteCount(postId);
        }
        // Fallback to memory if post ID not found
        return votingService.getDownvoteCount(post.getVote());
    }

    public boolean isEmoji(Post post) {
        return votingService.isEmoji(post.getVote());
    }

    public boolean isCommentDeleted(Post post, String id) {
        TreeMap<Integer, Comment> comments = post.getComments();

        int idx = Helper.extractFirstLevel(id);
        if (!comments.containsKey(idx)) {
            LoggerFacade.warning("Failed to check comment status with invalid comment ID: " + id);
            return true; // Consider non-existing comments as "deleted" for safety
        }

        String remaining_id = Helper.extractRemainingLevels(id);
        return commentService.isCommentDeleted(comments.get(idx), remaining_id);
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

    private Integer findNestedCommentId(Integer parentCommentId, String remainingPath) {
        // Helper method to find nested comment ID by traversing the path
        // This method recursively finds the database ID of a nested comment

        if (remainingPath.isEmpty()) {
            return parentCommentId;
        }

        int nextIndex = Helper.extractFirstLevel(remainingPath);
        String nextRemainingPath = Helper.extractRemainingLevels(remainingPath);

        // Find the nth reply to the parent comment
        String sql = "SELECT id FROM comments WHERE parent_comment_id = ? ORDER BY created_at LIMIT 1 OFFSET ?";

        try (java.sql.Connection conn = main.java.util.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, parentCommentId);
            stmt.setInt(2, nextIndex);
            java.sql.ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Integer nextCommentId = rs.getInt("id");
                // Recursively find the final nested comment
                return findNestedCommentId(nextCommentId, nextRemainingPath);
            }

        } catch (java.sql.SQLException e) {
            LoggerFacade.fatal("Error finding nested comment ID: " + e.getMessage());
        }

        return null;
    }
}
