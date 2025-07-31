package main.java.model;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

public class AppData {
    private HashMap<String, User> registeredUsers;
    private User loggedUser;
    private Integer idNextPost;
    private TreeMap<UUID, Post> loadedPosts;

    public AppData(TreeMap<UUID, Post> loadedPosts,
                   Integer idNextPost,
                   HashMap<String, User> registeredUsers) {
        this.registeredUsers = registeredUsers;
        loggedUser = null;
        this.idNextPost = idNextPost;
        this.loadedPosts = loadedPosts;
    }

    public HashMap<String, User> getRegisteredUsers() {
        return registeredUsers;
    }

    public User getLoggedUser() {
        return loggedUser;
    }
    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public TreeMap<UUID, Post> getLoadedPosts() {
        return loadedPosts;
    }
}
