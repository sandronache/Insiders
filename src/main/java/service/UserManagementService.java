package main.java.service;

import main.java.entity.Comment;
import main.java.entity.Post;
import main.java.entity.User;
import main.java.exceptions.UnauthorizedException;
import main.java.logger.LoggerFacade;
import main.java.model.AppData;
import main.java.repository.UserRepository;
import main.java.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Service responsible for user management operations
 */

@Service
public class UserManagementService {
    private final UserRepository userRepository;
    private final AppDataService appDataService;

    @Autowired
    private UserManagementService(AppDataService appDataService, UserRepository userRepository) {
        this.appDataService = appDataService;
        this.userRepository = userRepository;
    }


    public User findByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(() -> new UnauthorizedException("Utilizatorul"+ username + "nu a fost gasit"));
    }


    public boolean register(AppData appData, String username, String email, String password) {
        // Check in database first
        try {
            if (userRepository.existsByUsername(username)) {
                LoggerFacade.warning("Registration failed: Username '" + username + "' already exists");
                return false;
            }

            if (userRepository.existsByEmail(email)) {
                LoggerFacade.warning("Registration failed: Email '" + email + "' already exists");
                return false;
            }
        } catch (Exception e) {
            LoggerFacade.warning("Database check failed, checking in memory: " + e.getMessage());
            // Fallback to in-memory check
            if (appData.getRegisteredUsers().containsKey(username)) {
                LoggerFacade.warning("Registration failed: Username '" + username + "' already exists");
                return false;
            }
        }

        User user = new User(username, email, Helper.hashFunction(password));

        // Try to save to database
        try {
            userRepository.save(user);
            LoggerFacade.info("User saved to database: " + username);
        } catch (Exception e) {
            LoggerFacade.warning("Could not save user to database: " + e.getMessage());
        }

        // Update in-memory data
        appData.setLoggedUser(user);
        appData.getRegisteredUsers().put(username, user);

        LoggerFacade.info("New user registered: " + username);
        return true;
    }

    public boolean login(AppData appData, String username, String password) {
        User user = null;

        // Try to find user in database first
        try {
            var userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                user = userOpt.get();
                // Update in-memory data as well
                appData.getRegisteredUsers().put(username, user);
            }
        } catch (Exception e) {
            LoggerFacade.warning("Database lookup failed, checking in memory: " + e.getMessage());
        }

        // Fallback to in-memory data if database is not available
        if (user == null) {
            user = appData.getRegisteredUsers().get(username);
        }

        if (user != null && Helper.checkPassword(password, user.getHashedPassword())) {
            appData.setLoggedUser(user);
            LoggerFacade.info("User logged in: " + username);
            return true;
        }

        LoggerFacade.warning("Login failed for user: " + username);
        return false;
    }

    public void logout(AppData appData) {
        if (appData.getLoggedUser() != null) {
            LoggerFacade.info("User logged out: " + appData.getLoggedUser().getUsername());
        }
        appData.setLoggedUser(null);
    }
    //TODO: userdummy (com and posts appear deleted instead of being deleted properly) -> all references to dummy
    public void deleteUser(AppData appData) {
        String currUserUsername = appData.getLoggedUser().getUsername();
        appData.getRegisteredUsers().remove(currUserUsername);
        userRepository.deleteByUsername(currUserUsername);
        LoggerFacade.info("User account deleted: " + currUserUsername);
        Iterator<Map.Entry<UUID, Post>> iterator = appData.getLoadedPosts().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Post> entry = iterator.next();
            Post post = entry.getValue();
            if (post.getUsername().equals(currUserUsername)) {
                iterator.remove();
                LoggerFacade.info("Deleted post with id " + entry.getKey() + " by user " + currUserUsername);
            }
        }

        for (Post post : appData.getLoadedPosts().values()) {
            for (Comment comment : post.getComments().values()) {
                deleteComment(comment, currUserUsername);
            }
        }

        for (Post post : appData.getLoadedPosts().values()) {
            post.getVote().getUpvote().remove(currUserUsername);
            post.getVote().getDownvote().remove(currUserUsername);

            for (Comment comment : post.getComments().values()) {
                removeVote(comment, currUserUsername);
            }
        }
    }

    private void deleteComment(Comment comment, String username) {
        if (comment.getUsername().equals(username)) {
            comment.setIsDeleted(true); // contentul devine "[deleted]"
        }
        for (Comment reply : comment.getReplies().values()) {
            deleteComment(reply, username);
        }
    }

    private void removeVote(Comment comment, String username) {
        comment.getVote().getUpvote().remove(username);
        comment.getVote().getDownvote().remove(username);
        for (Comment reply : comment.getReplies().values()) {
            removeVote(reply, username);
        }
    }
}

