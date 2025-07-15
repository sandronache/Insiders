package model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AppData {
    private Map<String, User> registeredUsers;
    private User loggedUser;
    private LinkedList<Post> loadedPosts;

    // TODO change
    public AppData() {
        registeredUsers = new HashMap<>();
        loggedUser = null;
        loadedPosts = new LinkedList<>();
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

    public LinkedList<Post> getLoadedPosts() {
        return loadedPosts;
    }
}
