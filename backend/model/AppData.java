package model;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AppData {
    private Map<String, User> registeredUsers;
    private User loggedUser;
    private Integer idNextPost;
    private Map<Integer, Post> loadedPosts;

    // TODO change
    public AppData() {
        registeredUsers = new HashMap<>();
        loggedUser = null;
        idNextPost = 0;
        loadedPosts = new TreeMap<>();
    }

    public Map<String, User> getRegisteredUsers() {
        return registeredUsers;
    }

    public User getLoggedUser() {
        return loggedUser;
    }
    public void setLoggedUser(User loggedUser) {
        this.loggedUser = loggedUser;
    }

    public Integer getIdNextPost() {
        return idNextPost;
    }
    public void setIdNextPost(Integer id) {
        idNextPost = id;
    }

    public Map<Integer, Post> getLoadedPosts() {
        return loadedPosts;
    }
}
