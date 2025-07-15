package service;

import model.Post;
import model.AppData;
import model.User;

public class AppDataService {
    private final ContentService contentService;
    public AppDataService(ContentService contentService) {
        this.contentService = contentService;
    }

    public AppData createAppData() {
        // TODO init db
        return new AppData();
    }

    public boolean register(AppData appData, String username, String email, String password) {
        if (appData.getRegisteredUsers().containsKey(username)) {
            return false;
        }
        User user = new User(username, email, password);
        appData.setLoggedUser(user);
        appData.getRegisteredUsers().put(username, user);
        return true;
    }

    public boolean login(AppData appData, String username, String password) {
        User user = appData.getRegisteredUsers().get(username);
        if (user != null && user.checkPassword(password)) {
            appData.setLoggedUser(user);
            return true;
        }
        return false;
    }

    public void logout(AppData appData) {
        appData.setLoggedUser(null);
    }

    public void deleteUser(AppData appData) {
        String currUserUsername = appData.getLoggedUser().getUsername();
        appData.getRegisteredUsers().remove(currUserUsername);
    }

    public void addPost(AppData appData, String content) {
        Post newPost = contentService.createPost(content, appData.getLoggedUser().getUsername());
        appData.getLoadedPosts().add(newPost);
    }

    public void deletePost(AppData appData, int idx) {
        appData.getLoadedPosts().remove(idx);
    }

    // rendering function

    public String renderFeed(AppData appData) {
        int idx = 0;
        StringBuilder feed = new StringBuilder();
        for (Post post: appData.getLoadedPosts()) {
            feed.append(contentService.renderFeedPost(post, String.valueOf(idx))).append("\n");
            idx++;
        }
        return feed.toString();
    }
}

// 0 content upvotes downvotes
// 0.0 content upvotes downvotes
// 0.1 content upvotes downvotes
// 0.1.1 content upvotes downvotes
// 0.2 content upvotes downvotes

//public class UserService {
//    private Map<String, User> users;
//    private User currentUser;
//
//    public UserService() {
//        this.users = new HashMap<>();
//        this.currentUser = null;
//    }
//
//    public boolean register(String username, String email, String password) {
//        if (users.containsKey(username)) {
//            return false;
//        }
//        currentUser = new User(username, email, password);
//        users.put(username, currentUser);
//        return true;
//    }
//
//    public boolean login(String username, String password) {
//        User user = users.get(username);
//        if (user != null && user.checkPassword(password)) {
//            currentUser = user;
//            return true;
//        }
//        return false;
//    }
//
//    public void logout() {
//        currentUser = null;
//    }
//
//    public String getCurrUsername() {
//        return currentUser.getUsername();
//    }
//
//    public User getCurrentUser() {
//        return currentUser;
//    }
//
//    public void addUser(String username, String email, int hashedPassword) {
//        users.put(username, new User(username, email, hashedPassword));
//    }
//
//    public void deleteUser() {
//        users.remove(currentUser.getUsername());
//    }
//
//    public boolean isLoggedIn() {
//        return currentUser != null;
//    }
//}
