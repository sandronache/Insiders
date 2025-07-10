package login;

public class User {
    private final String username;
    private final String email;
    private final int hashedPassword;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.hashedPassword = password.hashCode();
    }

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

    public boolean checkPassword(String password) {
        return this.hashedPassword == password.hashCode();
    }

    @Override
    public String toString() {
        return "Login.User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}