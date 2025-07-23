package main.java.repository;

import main.java.util.DBConnection;
import main.java.logger.LoggerFacade;

import java.sql.*;

public class VoteRepository {

    // Post voting methods
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

    public int getPostUpvoteCount(Integer postId) {
        String sql = "SELECT COUNT(*) FROM post_votes WHERE post_id = ? AND is_upvote = true";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting post upvote count: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return 0;
    }

    public int getPostDownvoteCount(Integer postId) {
        String sql = "SELECT COUNT(*) FROM post_votes WHERE post_id = ? AND is_upvote = false";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting post downvote count: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return 0;
    }

    // Comment voting methods
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

    public int getCommentUpvoteCount(Integer commentId) {
        String sql = "SELECT COUNT(*) FROM comment_votes WHERE comment_id = ? AND is_upvote = true";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting comment upvote count: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return 0;
    }

    public int getCommentDownvoteCount(Integer commentId) {
        String sql = "SELECT COUNT(*) FROM comment_votes WHERE comment_id = ? AND is_upvote = false";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error getting comment downvote count: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return 0;
    }

    // Methods to get vote lists (needed for loading votes into memory)
    public java.util.List<String> getPostUpvotes(Integer postId) {
        java.util.List<String> upvotes = new java.util.ArrayList<>();
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

    public java.util.List<String> getPostDownvotes(Integer postId) {
        java.util.List<String> downvotes = new java.util.ArrayList<>();
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

    public java.util.List<String> getCommentUpvotes(Integer commentId) {
        java.util.List<String> upvotes = new java.util.ArrayList<>();
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

    public java.util.List<String> getCommentDownvotes(Integer commentId) {
        java.util.List<String> downvotes = new java.util.ArrayList<>();
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
}
