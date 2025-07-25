package main.java.service;

import main.java.logger.LoggerFacade;
import main.java.model.Comment;
import main.java.model.Post;
import main.java.model.Vote;
import main.java.repository.CommentRepository;
import main.java.repository.VoteRepository;
import main.java.util.Helper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

@Service
public class ContentService {
    private static ContentService instance;
    private final VotingService votingService;
    private final CommentService commentService;
    private final CommentRepository commentRepository;

    private ContentService(VotingService votingService,
                            CommentService commentService) {
        this.votingService = votingService;
        this.commentService = commentService;
        this.commentRepository = new CommentRepository();
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

        // For legacy compatibility, use default values for new fields
        String defaultTitle = content.length() > 50 ? content.substring(0, 50) + "..." : content;
        String defaultSubreddit = "general";

        return new Post(defaultTitle, content, username, defaultSubreddit);
    }

    // New method for creating posts with all required fields
    public Post createPost(String title, String content, String username, String subreddit) {
        LoggerFacade.debug("Creating new post for user: " + username + " in subreddit: " + subreddit);

        return new Post(title, content, username, subreddit);
    }

    private UUID findPostIdByContent(String content) {
        // This is a helper method to find post ID by content
        // In a real application, Post should have an ID field
        String sql = "SELECT id FROM posts WHERE content = ?";

        try (java.sql.Connection conn = main.java.util.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, content);
            java.sql.ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return (UUID) rs.getObject("id");
            }

        } catch (java.sql.SQLException e) {
            LoggerFacade.fatal("Error finding post ID: " + e.getMessage());
        }

        return null;
    }

    public void deleteCommentOrReply(Post post, String id) {
        LoggerFacade.info("Deleting comment/reply with ID: " + id);

        // Get post ID from database
        UUID postId = findPostIdByContent(post.getContent());
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
        UUID postId = findPostIdByContent(post.getContent());
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

    private Integer findCommentIdByIndex(UUID postId, Integer commentIndex) {
        // Helper method to find the database comment ID by post ID and comment index
        // This is a workaround since we're using TreeMap indices in memory
        String sql = "SELECT id FROM comments WHERE post_id = ? AND parent_comment_id IS NULL ORDER BY created_at LIMIT 1 OFFSET ?";

        try (java.sql.Connection conn = main.java.util.DBConnection.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, postId);
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

        // Also save to database and update emoji status
        UUID postId = findPostIdByContent(post.getContent());
        if (postId != null) {
            VoteRepository voteRepository = new VoteRepository();
            voteRepository.addPostUpvote(postId, username);

            // Check and save emoji status to database
            votingService.checkEmojiForPost(post.getVote(), postId);

            LoggerFacade.info("Post upvote saved to database for post ID: " + postId + " by user: " + username);
        } else {
            LoggerFacade.warning("Could not find post ID to save upvote to database");
        }
    }

    public void addDownvotePost(Post post, String username) {
        // Add downvote in memory
        votingService.addDownvote(post.getVote(), username);

        // Also save to database and update emoji status
        UUID postId = findPostIdByContent(post.getContent());
        if (postId != null) {
            VoteRepository voteRepository = new VoteRepository();
            voteRepository.addPostDownvote(postId, username);

            // Check and save emoji status to database
            votingService.checkEmojiForPost(post.getVote(), postId);

            LoggerFacade.info("Post downvote saved to database for post ID: " + postId + " by user: " + username);
        } else {
            LoggerFacade.warning("Could not find post ID to save downvote to database");
        }
    }

    public int getUpvoteCount(Post post) {
        // Use memory-based voting for better performance
        // Votes are loaded and synchronized when posts are loaded from database
        return votingService.getUpvoteCount(post.getVote());
    }
    public int getDownvoteCount(Post post) {
        // Use memory-based voting for better performance
        // Votes are loaded and synchronized when posts are loaded from database
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

    // Load votes from database into memory objects
    public void loadVotesForPost(Post post, UUID postId) {
        try {
            VoteRepository voteRepository = new VoteRepository();

            // Get upvotes from database
            List<String> upvotes = voteRepository.getPostUpvotes(postId);
            // Get downvotes from database
            List<String> downvotes = voteRepository.getPostDownvotes(postId);

            // Load votes into the post's Vote object in memory
            Vote vote = post.getVote();
            for (String username : upvotes) {
                vote.getUpvote().add(username);
            }
            for (String username : downvotes) {
                vote.getDownvote().add(username);
            }

            // Update emoji status based on loaded votes
            votingService.checkEmoji(vote);

            LoggerFacade.debug("Loaded " + upvotes.size() + " upvotes and " + downvotes.size() + " downvotes for post");

        } catch (Exception e) {
            LoggerFacade.warning("Error loading votes for post: " + e.getMessage());
        }
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
