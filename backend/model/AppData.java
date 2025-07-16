package model;

import java.util.HashMap;
import java.util.TreeMap;

public class AppData {
    private HashMap<String, User> registeredUsers;
    private User loggedUser;
    private Integer idNextPost;
    private TreeMap<Integer, Post> loadedPosts;

    // TODO change
    public AppData(TreeMap<Integer, Post> loadedPosts,
                   Integer idNextPost) {
        registeredUsers = new HashMap<>();
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

    public Integer getIdNextPost() {
        return idNextPost;
    }
    public void setIdNextPost(Integer id) {
        idNextPost = id;
    }

    public TreeMap<Integer, Post> getLoadedPosts() {
        return loadedPosts;
    }
}
