package Login;

public class User {
    private String username;
    private String email;
    private int hashedPassword;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.hashedPassword = password.hashCode();
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