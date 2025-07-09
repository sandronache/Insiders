package mihnea;

import Login.User;
import Login.UserService;

import java.util.Scanner;

public class AuthCLI {
    private static final UserService userService = new UserService();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        System.out.println("Welcome to Auth CLI");

        while (running) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    register();
                    break;
                case "2":
                    login();
                    break;
                case "3":
                    logout();
                    break;
                case "4":
                    displayCurrentUser();
                    break;
                case "5":
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n--- Auth Menu ---");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Logout");
        System.out.println("4. Display current user");
        System.out.println("5. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void register() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (userService.register(username, email, password)) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Username already exists!");
        }
    }

    private static void login() {
        if (userService.isLoggedIn()) {
            System.out.println("You are already logged in!");
            return;
        }

        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (userService.login(username, password)) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid username or password!");
        }
    }

    private static void logout() {
        if (userService.logout()) {
            System.out.println("Logout successful!");
        } else {
            System.out.println("You are not logged in!");
        }
    }

    private static void displayCurrentUser() {
        User currentUser = userService.getCurrentUser();
        if (currentUser != null) {
            System.out.println("Current user: " + currentUser);
        } else {
            System.out.println("No user is currently logged in.");
        }
    }
}