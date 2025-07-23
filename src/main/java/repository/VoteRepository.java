package main.java.repository;

import main.java.util.DBConnection;
import main.java.logger.LoggerFacade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoteRepository {

    public void addPostUpvote(Integer postId, String username) {
        String sql = "INSERT INTO post_votes (post_id, username, is_upvote) VALUES (?, ?, true) " +
                    "ON CONFLICT (post_id, username) DO UPDATE SET is_upvote = true";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            stmt.setString(2, username);
            stmt.executeUpdate();

            LoggerFacade.info("Upvote added for post " + postId + " by user " + username);

        } catch (SQLException e) {
            LoggerFacade.fatal("Error adding post upvote: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void addPostDownvote(Integer postId, String username) {
        String sql = "INSERT INTO post_votes (post_id, username, is_upvote) VALUES (?, ?, false) " +
                    "ON CONFLICT (post_id, username) DO UPDATE SET is_upvote = false";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            stmt.setString(2, username);
            stmt.executeUpdate();

            LoggerFacade.info("Downvote added for post " + postId + " by user " + username);

        } catch (SQLException e) {
            LoggerFacade.fatal("Error adding post downvote: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void removePostVote(Integer postId, String username) {
        String sql = "DELETE FROM post_votes WHERE post_id = ? AND username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                LoggerFacade.info("Vote removed for post " + postId + " by user " + username);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error removing post vote: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void addCommentUpvote(Integer commentId, String username) {
        String sql = "INSERT INTO comment_votes (comment_id, username, is_upvote) VALUES (?, ?, true) " +
                    "ON CONFLICT (comment_id, username) DO UPDATE SET is_upvote = true";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            stmt.setString(2, username);
            stmt.executeUpdate();

            LoggerFacade.info("Upvote added for comment " + commentId + " by user " + username);

        } catch (SQLException e) {
            LoggerFacade.fatal("Error adding comment upvote: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void addCommentDownvote(Integer commentId, String username) {
        String sql = "INSERT INTO comment_votes (comment_id, username, is_upvote) VALUES (?, ?, false) " +
                    "ON CONFLICT (comment_id, username) DO UPDATE SET is_upvote = false";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            stmt.setString(2, username);
            stmt.executeUpdate();

            LoggerFacade.info("Downvote added for comment " + commentId + " by user " + username);

        } catch (SQLException e) {
            LoggerFacade.fatal("Error adding comment downvote: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void removeCommentVote(Integer commentId, String username) {
        String sql = "DELETE FROM comment_votes WHERE comment_id = ? AND username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                LoggerFacade.info("Vote removed for comment " + commentId + " by user " + username);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error removing comment vote: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<String> getPostUpvotes(Integer postId) {
        List<String> upvotes = new ArrayList<>();
        String sql = "SELECT username FROM post_votes WHERE post_id = ? AND is_upvote = true";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                upvotes.add(rs.getString("username"));
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting post upvotes: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return upvotes;
    }

    public List<String> getPostDownvotes(Integer postId) {
        List<String> downvotes = new ArrayList<>();
        String sql = "SELECT username FROM post_votes WHERE post_id = ? AND is_upvote = false";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                downvotes.add(rs.getString("username"));
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting post downvotes: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return downvotes;
    }

    public List<String> getCommentUpvotes(Integer commentId) {
        List<String> upvotes = new ArrayList<>();
        String sql = "SELECT username FROM comment_votes WHERE comment_id = ? AND is_upvote = true";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                upvotes.add(rs.getString("username"));
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting comment upvotes: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return upvotes;
    }

    public List<String> getCommentDownvotes(Integer commentId) {
        List<String> downvotes = new ArrayList<>();
        String sql = "SELECT username FROM comment_votes WHERE comment_id = ? AND is_upvote = false";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                downvotes.add(rs.getString("username"));
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting comment downvotes: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return downvotes;
    }

    public int getUserPostVote(Integer postId, String username) {
        String sql = "SELECT is_upvote FROM post_votes WHERE post_id = ? AND username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("is_upvote") ? 1 : -1;
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting user post vote: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return 0; // No vote
    }

    public int getUserCommentVote(Integer commentId, String username) {
        String sql = "SELECT is_upvote FROM comment_votes WHERE comment_id = ? AND username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("is_upvote") ? 1 : -1;
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting user comment vote: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return 0; // No vote
    }

    public void setPostEmojiFlag(Integer postId, boolean isEmoji) {
        String sql = "INSERT INTO post_emoji_flags (post_id, is_emoji) VALUES (?, ?) " +
                    "ON CONFLICT (post_id) DO UPDATE SET is_emoji = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            stmt.setBoolean(2, isEmoji);
            stmt.setBoolean(3, isEmoji);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LoggerFacade.fatal("Error setting post emoji flag: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void setCommentEmojiFlag(Integer commentId, boolean isEmoji) {
        String sql = "INSERT INTO comment_emoji_flags (comment_id, is_emoji) VALUES (?, ?) " +
                    "ON CONFLICT (comment_id) DO UPDATE SET is_emoji = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            stmt.setBoolean(2, isEmoji);
            stmt.setBoolean(3, isEmoji);
            stmt.executeUpdate();

        } catch (SQLException e) {
            LoggerFacade.fatal("Error setting comment emoji flag: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean getPostEmojiFlag(Integer postId) {
        String sql = "SELECT is_emoji FROM post_emoji_flags WHERE post_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("is_emoji");
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting post emoji flag: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return false; // Default to false
    }

    public boolean getCommentEmojiFlag(Integer commentId) {
        String sql = "SELECT is_emoji FROM comment_emoji_flags WHERE comment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("is_emoji");
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting comment emoji flag: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return false; // Default to false
    }
}
