package Services;

import login.User;

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
        currentUser = new User(username, email, password);
        users.put(username, currentUser);
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

    public void logout() {
        currentUser = null;
    }

    public String getCurrUsername() {
        return currentUser.getUsername();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void addUser(String username, String email, int hashedPassword) {
        users.put(username, new User(username, email, hashedPassword));
    }

    public void deleteUser() {
        users.remove(currentUser.getUsername());
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}