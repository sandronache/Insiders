package main.java.repository;

import main.java.model.Post;
import main.java.model.Vote;
import main.java.util.DBConnection;
import main.java.logger.LoggerFacade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostRepository {

    public void save(Post post) {
        String sql = "INSERT INTO posts (content, username, id_next_comment) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, post.getContent());
            stmt.setString(2, post.getUsername());
            stmt.setInt(3, post.getIdNextComment());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int generatedId = rs.getInt("id");
                LoggerFacade.info("Post saved with ID: " + generatedId + " by user: " + post.getUsername());
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error saving post: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Optional<Post> findById(Integer id) {
        String sql = "SELECT id, content, username, id_next_comment, created_at FROM posts WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Vote vote = new Vote(); // Will be populated by VoteRepository
                Post post = new Post(
                    rs.getString("content"),
                    rs.getString("username"),
                    vote
                );
                post.setIdNextComment(rs.getInt("id_next_comment"));
                return Optional.of(post);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding post by ID: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    public Optional<Post> findByContent(String content) {
        String sql = "SELECT id, content, username, id_next_comment, created_at FROM posts WHERE content = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, content);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Vote vote = new Vote(); // Will be populated by VoteRepository
                Post post = new Post(
                    rs.getString("content"),
                    rs.getString("username"),
                    vote
                );
                post.setIdNextComment(rs.getInt("id_next_comment"));
                return Optional.of(post);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding post by content: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    public List<Post> findAllOrderedByDate() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT id, content, username, id_next_comment, created_at FROM posts ORDER BY created_at ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vote vote = new Vote(); // Will be populated by VoteRepository
                Post post = new Post(
                    rs.getString("content"),
                    rs.getString("username"),
                    vote
                );
                post.setIdNextComment(rs.getInt("id_next_comment"));
                posts.add(post);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding all posts: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return posts;
    }

    // New method to get posts with their database IDs
    public List<PostWithId> findAllOrderedByDateWithIds() {
        List<PostWithId> postsWithIds = new ArrayList<>();
        String sql = "SELECT id, content, username, id_next_comment, created_at FROM posts ORDER BY created_at ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Vote vote = new Vote(); // Will be populated by VoteRepository
                Post post = new Post(
                    rs.getString("content"),
                    rs.getString("username"),
                    vote
                );
                post.setIdNextComment(rs.getInt("id_next_comment"));

                PostWithId postWithId = new PostWithId(rs.getInt("id"), post);
                postsWithIds.add(postWithId);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding all posts with IDs: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return postsWithIds;
    }

    public List<Post> findByUsername(String username) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT id, content, username, id_next_comment, created_at FROM posts WHERE username = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vote vote = new Vote(); // Will be populated by VoteRepository
                Post post = new Post(
                    rs.getString("content"),
                    rs.getString("username"),
                    vote
                );
                post.setIdNextComment(rs.getInt("id_next_comment"));
                posts.add(post);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding posts by username: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return posts;
    }

    public void deleteById(Integer id) {
        String sql = "DELETE FROM posts WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                LoggerFacade.info("Post deleted: " + id);
            } else {
                LoggerFacade.warning("No post found to delete: " + id);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error deleting post: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void update(Post post, Integer id) {
        String sql = "UPDATE posts SET content = ?, id_next_comment = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, post.getContent());
            stmt.setInt(2, post.getIdNextComment());
            stmt.setInt(3, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                LoggerFacade.info("Post updated: " + id);
            } else {
                LoggerFacade.warning("No post found to update: " + id);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error updating post: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Helper class to carry both database ID and Post object
    public static class PostWithId {
        private final Integer databaseId;
        private final Post post;

        public PostWithId(Integer databaseId, Post post) {
            this.databaseId = databaseId;
            this.post = post;
        }

        public Integer getDatabaseId() {
            return databaseId;
        }

        public Post getPost() {
            return post;
        }
    }
}
