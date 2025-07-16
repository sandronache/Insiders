package service;

import logger.LoggerFacade;
import model.AppData;
import model.Post;
import model.User;
import util.Helper;

import java.util.TreeMap;

public class AppDataService {
    private final FilesService filesService;
    private final ContentService contentService;
    public AppDataService(FilesService filesService,
                          ContentService contentService) {
        this.filesService = filesService;
        this.contentService = contentService;

        LoggerFacade.debug("AppDataService initialized");
    }

    public AppData createAppData() {
        // TODO init db
        LoggerFacade.info("Creating new application data");

        TreeMap<Integer, Post> loadedPosts = filesService.loadPosts();

        int idNextPost = 0;
        if (!loadedPosts.isEmpty()) {
            idNextPost = loadedPosts.lastKey() + 1;
        }

        return new AppData(loadedPosts, idNextPost);
    }

    public void writeAppData(AppData appData) {
        filesService.writePosts(appData.getLoadedPosts());
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