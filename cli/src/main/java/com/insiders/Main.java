package com.insiders;

import com.insiders.clients.AuthClient;
import com.insiders.clients.PostClient;
import com.insiders.clients.SubredditClient;
import com.insiders.menu.AuthMenu;
import com.insiders.menu.FeedMenu;
import com.insiders.session.SessionManager;
import com.insiders.util.ConsoleIO;
import com.insiders.util.MenuFormatter;

public class Main {
    public static void main(String[] args) {
        String base = System.getenv().getOrDefault("API_BASE_URL", "http://ec2-3-74-161-90.eu-central-1.compute.amazonaws.com:8080");

        var session = new SessionManager();
        var authClient = new AuthClient(base, session::authHeaders);
        var postClient = new PostClient(base, session::authHeaders);
        var subredditClient = new SubredditClient(base, session::authHeaders);

        var authMenu = new AuthMenu(authClient, session);
        var feedMenu = new FeedMenu(postClient, subredditClient, session);

        while(true){
            if (session.isLoggedIn()) {
                showMainMenu(session, feedMenu);
            } else {
                showWelcomeMenu(authMenu, feedMenu);
            }
        }
    }

    private static void showMainMenu(SessionManager session, FeedMenu feedMenu) {
        MenuFormatter.printMenuHeader("Insiders Main Menu");
        MenuFormatter.printMenuOptions(
            "1. Browse Posts (Feed)",
            "2. Logout",
            "0. Exit"
        );

        int choice = ConsoleIO.readIntInRange("Enter your choice: ", 0, 2);
        switch (choice){
            case 1 -> feedMenu.showMenu();
            case 2 -> {
                session.logout();
                MenuFormatter.printSuccessMessage("Logged out successfully!");
                MenuFormatter.printInfoMessage("Thank you for using Insiders!");
            }
            case 0 -> {
                MenuFormatter.printInfoMessage("See you next time!");
                MenuFormatter.printInfoMessage("Thank you for using Insiders!");
                System.exit(0);
            }
            default -> MenuFormatter.printErrorMessage("Invalid choice. Please try again!");
        }
    }

    private static void showWelcomeMenu(AuthMenu authMenu, FeedMenu feedMenu) {
        MenuFormatter.printMenuHeader("Welcome to Insiders");
        MenuFormatter.printInfoMessage("Please authenticate to access the platform");
        MenuFormatter.printMenuOptions(
            "1. Authenticate",
            "0. Exit"
        );

        int choice = ConsoleIO.readIntInRange("Enter your choice: ", 0, 1);
        switch (choice){
            case 1 -> {
                if (authMenu.showMenu()) {
                    MenuFormatter.printSuccessMessage("Authentication successful!");
                    MenuFormatter.printInfoMessage("Redirecting to feed...");
                    feedMenu.showMenu();
                }
            }
            case 0 -> {
                MenuFormatter.printInfoMessage("See you next time!");
                MenuFormatter.printInfoMessage("Thank you for visiting Insiders!");
                System.exit(0);
            }
            default -> MenuFormatter.printErrorMessage("Invalid choice. Please try again!");
        }
    }
}