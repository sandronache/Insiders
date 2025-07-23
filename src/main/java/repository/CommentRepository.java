package main.java.repository;

import main.java.model.Comment;
import main.java.model.Vote;
import main.java.util.DBConnection;
import main.java.logger.LoggerFacade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CommentRepository {

    public Integer save(Comment comment, Integer postId, Integer parentCommentId) {
        String sql = "INSERT INTO comments (post_id, parent_comment_id, content, username, id_next_reply, is_deleted) " +
                    "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            if (parentCommentId != null) {
                stmt.setInt(2, parentCommentId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, comment.getContent());
            stmt.setString(4, comment.getUsername());
            stmt.setInt(5, comment.getIdNextReply());
            stmt.setBoolean(6, comment.isDeleted());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int generatedId = rs.getInt("id");
                LoggerFacade.info("Comment saved with ID: " + generatedId + " by user: " + comment.getUsername());
                return generatedId;
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error saving comment: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return null;
    }

    public Optional<Comment> findById(Integer id) {
        String sql = "SELECT id, post_id, parent_comment_id, content, username, id_next_reply, is_deleted, created_at " +
                    "FROM comments WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Vote vote = new Vote(); // Will be populated by VoteRepository
                Comment comment = new Comment(
                    rs.getString("content"),
                    rs.getString("username"),
                    vote
                );
                comment.setIdNextReply(rs.getInt("id_next_reply"));
                comment.setIsDeleted(rs.getBoolean("is_deleted"));
                return Optional.of(comment);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding comment by ID: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    public List<Comment> findByPostId(Integer postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT id, post_id, parent_comment_id, content, username, id_next_reply, is_deleted, created_at " +
                    "FROM comments WHERE post_id = ? AND parent_comment_id IS NULL ORDER BY created_at";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vote vote = new Vote(); // Will be populated by VoteRepository
                Comment comment = new Comment(
                    rs.getString("content"),
                    rs.getString("username"),
                    vote
                );
                comment.setIdNextReply(rs.getInt("id_next_reply"));
                comment.setIsDeleted(rs.getBoolean("is_deleted"));
                comment.setDatabaseId(rs.getInt("id")); // Set the database ID
                comments.add(comment);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding comments by post ID: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return comments;
    }

    public List<Comment> findRepliesByParentId(Integer parentCommentId) {
        List<Comment> replies = new ArrayList<>();
        String sql = "SELECT id, post_id, parent_comment_id, content, username, id_next_reply, is_deleted, created_at " +
                    "FROM comments WHERE parent_comment_id = ? ORDER BY created_at";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, parentCommentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vote vote = new Vote(); // Will be populated by VoteRepository
                Comment comment = new Comment(
                    rs.getString("content"),
                    rs.getString("username"),
                    vote
                );
                comment.setIdNextReply(rs.getInt("id_next_reply"));
                comment.setIsDeleted(rs.getBoolean("is_deleted"));
                comment.setDatabaseId(rs.getInt("id")); // Set the database ID for replies too
                replies.add(comment);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding replies by parent ID: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return replies;
    }

    public void markAsDeleted(Integer id) {
        String sql = "UPDATE comments SET is_deleted = true WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                LoggerFacade.info("Comment marked as deleted: " + id);
            } else {
                LoggerFacade.warning("No comment found to delete: " + id);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error marking comment as deleted: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void update(Comment comment, Integer id) {
        String sql = "UPDATE comments SET content = ?, id_next_reply = ?, is_deleted = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, comment.getContent());
            stmt.setInt(2, comment.getIdNextReply());
            stmt.setBoolean(3, comment.isDeleted());
            stmt.setInt(4, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                LoggerFacade.info("Comment updated: " + id);
            } else {
                LoggerFacade.warning("No comment found to update: " + id);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error updating comment: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Integer getNextCommentId(Integer postId) {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 as next_id FROM comments WHERE post_id = ? AND parent_comment_id IS NULL";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("next_id");
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting next comment ID: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return 1; // Default to 1 if no comments exist
    }

    public Integer getNextReplyId(Integer commentId) {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 as next_id FROM comments WHERE parent_comment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("next_id");
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting next reply ID: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return 1; // Default to 1 if no replies exist
    }

    public boolean existsById(Integer id) {
        String sql = "SELECT 1 FROM comments WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            LoggerFacade.fatal("Error checking if comment exists: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Comment> findByUsername(String username) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT id, post_id, parent_comment_id, content, username, id_next_reply, is_deleted, created_at " +
                    "FROM comments WHERE username = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vote vote = new Vote(); // Will be populated by VoteRepository
                Comment comment = new Comment(
                    rs.getString("content"),
                    rs.getString("username"),
                    vote
                );
                comment.setIdNextReply(rs.getInt("id_next_reply"));
                comment.setIsDeleted(rs.getBoolean("is_deleted"));
                comments.add(comment);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding comments by username: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return comments;
    }

    public List<Comment> findAllByPostId(Integer postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT id, post_id, parent_comment_id, content, username, id_next_reply, is_deleted, created_at " +
                    "FROM comments WHERE post_id = ? ORDER BY created_at";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vote vote = new Vote(); // Will be populated by VoteRepository
                Comment comment = new Comment(
                    rs.getString("content"),
                    rs.getString("username"),
                    vote
                );
                comment.setIdNextReply(rs.getInt("id_next_reply"));
                comment.setIsDeleted(rs.getBoolean("is_deleted"));
                comment.setDatabaseId(rs.getInt("id")); // Store the database ID
                comments.add(comment);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding all comments by post ID: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return comments;
    }
}
