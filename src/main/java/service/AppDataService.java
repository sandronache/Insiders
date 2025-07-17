package main.java.service;

import main.java.logger.LoggerFacade;
import main.java.model.AppData;
import main.java.model.Post;
import main.java.model.User;
import main.java.util.Helper;

import java.util.HashMap;
import java.util.TreeMap;

public class AppDataService {
    private static AppDataService instance;
    private final FilesService filesService;
    private final ContentService contentService;

    private AppDataService(FilesService filesService,
                           ContentService contentService) {
        this.filesService = filesService;
        this.contentService = contentService;
    }

    public static AppDataService getInstance() {
        if (instance == null) {
            instance = new AppDataService(FilesService.getInstance(),
                                            ContentService.getInstance());
        }
        return instance;
    }

    public AppData createAppData() {
        LoggerFacade.info("Creating new application data");

        TreeMap<Integer, Post> loadedPosts = filesService.loadPosts();

        int idNextPost = 0;
        if (!loadedPosts.isEmpty()) {
            idNextPost = loadedPosts.lastKey() + 1;
        }

        HashMap<String, User> registeredUsers = filesService.loadUsers();

        return new AppData(loadedPosts, idNextPost, registeredUsers);
    }

    public void writeAppData(AppData appData) {
        filesService.writePosts(appData.getLoadedPosts());
        filesService.writeUsers(appData.getRegisteredUsers());
    }

    public boolean register(AppData appData, String username, String email, String password) {
        if (appData.getRegisteredUsers().containsKey(username)) {
            LoggerFacade.warning("Registration failed: Username '" + username + "' already exists");
            return false;
        }
        User user = new User(username, email, Helper.hashFunction(password));
        appData.setLoggedUser(user);
        appData.getRegisteredUsers().put(username, user);
        LoggerFacade.info("New user registered: " + username);
        return true;
    }

    public boolean login(AppData appData, String username, String password) {
        User user = appData.getRegisteredUsers().get(username);
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

    public void deleteUser(AppData appData) {
        String currUserUsername = appData.getLoggedUser().getUsername();
        appData.getRegisteredUsers().remove(currUserUsername);
        LoggerFacade.info("User account deleted: " + currUserUsername);
    }

    public void addPost(AppData appData, String content) {
        String username = appData.getLoggedUser().getUsername();
        Post post = contentService.createPost(content, username);

        Integer id = appData.getIdNextPost();
        appData.setIdNextPost(id + 1);

        appData.getLoadedPosts().put(id, post);

        LoggerFacade.info("New post created by user: " + username);
    }

    public void deletePost(AppData appData, int idx) {
        if (appData.getLoadedPosts().containsKey(idx)) {
            LoggerFacade.info("Post with index " + idx + " deleted");

            appData.getLoadedPosts().remove(idx);
        } else {
            LoggerFacade.warning("Attempt to delete non-existent post at index: " + idx);
        }
    }

    // rendering function

    public String renderFeed(AppData appData) {
        LoggerFacade.debug("Rendering feed with " + appData.getLoadedPosts().size() + " posts");

        StringBuilder feed = new StringBuilder();

        appData.getLoadedPosts().forEach((id, post) ->
                feed.append(contentService.renderFeedPost(post, id.toString()))
                        .append("\n"));

        return feed.toString();
    }
}