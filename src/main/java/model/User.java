package main.java.model;

public class User {
    private final String username;
    private final String email;
    private final int hashedPassword;

    public User(String username, String email, int hashedPassword) {
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getHashedPassword() {
        return hashedPassword;
    }
}