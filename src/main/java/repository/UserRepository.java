package main.java.repository;

import main.java.entity.User;
import main.java.util.DBConnection;
import main.java.logger.LoggerFacade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    public void save(User user) {
        String sql = "INSERT INTO users (username, email, hashed_password) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getHashedPassword());

            stmt.executeUpdate();
            LoggerFacade.info("User saved: " + user.getUsername());

        } catch (SQLException e) {
            LoggerFacade.fatal("Error saving user: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT username, email, hashed_password FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getInt("hashed_password")
                );
                return Optional.of(user);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding user by username: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT username, email, hashed_password FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getInt("hashed_password")
                );
                return Optional.of(user);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding user by email: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            LoggerFacade.fatal("Error checking if user exists by username: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            LoggerFacade.fatal("Error checking if user exists by email: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteByUsername(String username) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                LoggerFacade.info("User deleted: " + username);
            } else {
                LoggerFacade.warning("No user found to delete: " + username);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error deleting user: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT username, email, hashed_password FROM users ORDER BY created_at";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User(
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getInt("hashed_password")
                );
                users.add(user);
            }

        } catch (SQLException e) {
            LoggerFacade.fatal("Error finding all users: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return users;
    }

}
