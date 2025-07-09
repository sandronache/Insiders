package App;

import Post.Post;
import Login.User;

import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class App {
    private static Map<String, Post> posts;
    private static Map<String, User> users;
    private static User currentUser;
    private static boolean isLoggedIn;
    private static final App INSTANCE = null;
    private App() {
        posts = new HashMap<>();
        users = new HashMap<>();
        currentUser = null;
        isLoggedIn = false;
    }
    public static App getInstance() {
        return new App();
    }
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

    public void run() {
        Scanner input = new Scanner(System.in);
        while (true) {
            if (currentUser == null) { // not yet logged in
                boolean wrongChoice = true;
                while (wrongChoice) {
                    System.out.println("1. Register");
                    System.out.println("2. Login");
                    System.out.println(">>>");
                    String choice = input.nextLine();
                    switch (choice) {
                        case "1":
                            boolean isRegistered = false;
                            do {
                                System.out.println("Enter username: ");
                                String username = input.nextLine();
                                System.out.println("Enter email: ");
                                String email = input.nextLine();
                                System.out.println("Enter password: ");
                                String password = input.nextLine();
                                isRegistered = register(username, email, password);
                                if (isRegistered)
                                    System.out.println("You have successfully registered!");
                                else
                                    System.out.println("Username already exists!");
                            } while (!isRegistered);
                            wrongChoice = false;
                            break;
                        case "2":
                            boolean isLoggedIn = false;
                            do {
                                System.out.println("Enter username: ");
                                String username = input.nextLine();
                                System.out.println("Enter password: ");
                                String password = input.nextLine();
                                isLoggedIn = login(username, password);
                                if (isLoggedIn)
                                    System.out.println("You have successfully logged in!");
                                else
                                    System.out.println("Wrong username or password!");
                            } while (!isLoggedIn);
                            wrongChoice = false;
                            break;
                        default:
                            System.out.println("Invalid choice");
                            break;
                    }
                    for (int i = 0; i < 10; i++) {
                        System.out.println(); // simulates terminal clear
                    }
                }
            }
            else {
                for (int i = 0; i < posts.size(); i++) { // displays the feed
                    posts[i].display();
                }
                boolean wrongChoice = true;
                while (wrongChoice) {
                    System.out.println("1. Create a new post");
                    System.out.println("2. Comment a post");
                    System.out.println("3. Up/down vote a post");
                    System.out.println("4. Delete current user");
                    System.out.println("5. Logout");
                    System.out.println(">>>");
                    String choice = input.nextLine();
                    switch (choice) {
                        case "1":
                            System.out.println("Enter the content of the post");
                            String content = input.nextLine();
                            posts.put(currentUser.getUsername(), new Post(content, currentUser.getUsername()));
                            wrongChoice = false;
                            break;
                        case "2":
                            System.out.println("Enter the id of the post you want to comment");
                            String id = input.nextLine();
                            // TODO
                            wrongChoice = false;
                            break;
                        case "3":
                            System.out.println("Enter the id of the post you want to vote");
                            String post_id = input.nextLine();
                            do {
                                System.out.println("Do you want to upvote or downvote this post? (1/0");
                                String vote = input.nextLine();
                                if (vote != "0" || vote != "1")
                                    System.out.println("Invalid choice");
                            } while (vote != "0" || vote != "1");
                            // TODO
                            wrongChoice = false;
                            break;
                        case "4":
                            deleteUser();
                            System.out.println("Deleted current user");
                            wrongChoice = false;
                            break;
                        case "5":
                            currentUser = null;
                            isLoggedIn = false;
                            System.out.println("You have been logged out");
                            wrongChoice = false;
                            break;
                        default:
                            System.out.println("Invalid choice");
                            break;
                    }
                    for (int i = 0; i < 10; i++) {
                        System.out.println(); // simulates terminal clear
                    }
                }
            }
        }
    }

    public static void deleteUser() {

    }
}
