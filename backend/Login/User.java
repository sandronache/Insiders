package Login;

public class User {
    private String username;
    private String email;
    private String password;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    @Override
    public String toString() {
        return "Login.User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}