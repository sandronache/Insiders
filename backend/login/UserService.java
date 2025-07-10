package login;

import java.util.HashMap;
import java.util.Map;

public class UserService {
    private Map<String, User> users;
    private User currentUser;

    public UserService() {
        this.users = new HashMap<>();
        this.currentUser = null;
    }

    public boolean register(String username, String email, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, new User(username, email, password));
        return true;
    }

    public boolean login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.checkPassword(password)) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public boolean logout() {
        if (currentUser != null) {
            currentUser = null;
            return true;
        }
        return false;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}