package App;

import Post.Post;
import Post.PostRenderer;
import Login.User;

import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class App {
    private Map<String, Post> posts;
    private Map<String, User> users;
    private User currentUser;
    private static final App INSTANCE = new App();
    private Scanner input = new Scanner(System.in);
    private App() {
        posts = new HashMap<>();
        users = new HashMap<>();
        currentUser = null;
    }
    public static App getInstance() {
        return INSTANCE;
    } // created as a singleton
    public boolean register(String username, String email, String password) {
        if (users.containsKey(username))
            return false;
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

    private void deleteCurrentUser() {
        users.remove(currentUser.getUsername());
        logout();
        // ++ erasing all posted content (posts, replies, comments)
    }

    private void registerPrompt() {
        System.out.println("Enter username: ");
        String username = input.nextLine();
        System.out.println("Enter email: ");
        String email = input.nextLine();
        System.out.println("Enter password: ");
        String password = input.nextLine();

        boolean isRegistered = register(username, email, password);

        if (isRegistered) {
            System.out.println("You have successfully registered!");
        } else {
            System.out.println("Username already exists!");
            registerPrompt();
        }
    }

    private void loginPrompt() {
        System.out.println("Enter username: ");
        String username = input.nextLine();
        System.out.println("Enter password: ");
        String password = input.nextLine();

        boolean isLoggedIn = login(username, password);

        if (isLoggedIn) {
            System.out.println("You have successfully logged in!");
        } else {
            System.out.println("Wrong username or password!");
            loginPrompt();
        }
    }

    private void authenticationPrompt() {
        boolean doneAuthentication = false;
        while(!doneAuthentication) {
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println(">>>");
            String choice = input.nextLine();
            switch (choice) {
                case "1":
                    registerPrompt();
                    doneAuthentication = true;
                    break;
                case "2":
                    loginPrompt();
                    doneAuthentication = true;
                    break;
                default:
                    System.out.println("Invalid choice");
                    break;
            }
            clearCLI();
        }
    }

    private void clearCLI() {
        for (int i = 0; i < 10; i++) {
            System.out.println(); // simulates terminal clear
        }
    }

    private void showFeed() {
        int idx = 0;
        for (Post post: posts.values()) {
            System.out.println(PostRenderer.renderFeedPost(post, String.valueOf(idx)));
            idx++;
        }
    }

    private void addNewPostPrompt() {
        System.out.println("Enter the content of the post");
        String content = input.nextLine();

        posts.put(currentUser.getUsername(), new Post(content, currentUser.getUsername()));
    }

    private void deleteCurrentUserPrompt() {
        deleteCurrentUser();
        System.out.println("Deleted current user");
    }

    private void logoutPrompt() {
        logout();
        System.out.println("You have been logged out");
    }

    private void feedPrompt() {
        boolean onFeed = true;
        while(onFeed) {
            showFeed();
            System.out.println("1. Create a new post");
            System.out.println("2. Enter a post");
            System.out.println("3. Delete current user");
            System.out.println("4. Logout");
            System.out.println(">>>");
            String choice = input.nextLine();
            switch (choice) {
                case "1":
                    addNewPostPrompt();
                    break;
                case "2":
                    break;
                case "3":
                    deleteCurrentUserPrompt();
                    onFeed = false;
                    break;
                case "4":
                    logoutPrompt();
                    onFeed = false;
                    break;
                default:
                    System.out.println("Invalid choice");
                    break;
            }
            clearCLI();
        }
    }

    public void run(){
        while (true) {
            authenticationPrompt();
            feedPrompt();
        }
    }
}
