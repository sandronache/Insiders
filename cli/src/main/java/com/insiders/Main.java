package com.insiders;

import com.insiders.clients.AuthClient;
import com.insiders.clients.PostClient;
import com.insiders.clients.SubredditClient;
import com.insiders.menu.AuthMenu;
import com.insiders.menu.FeedMenu;
import com.insiders.session.SessionManager;
import com.insiders.util.ConsoleIO;


public class Main {
    public static void main(String[] args) {
        String base = System.getenv().getOrDefault("API_BASE_URL", "http://localhost:8080");
        var session = new SessionManager();
        var authClient = new AuthClient(base, session::authHeaders);
        var postClient = new PostClient(base, session::authHeaders);
        var subredditClient = new SubredditClient(base, session::authHeaders);
        var authMenu = new AuthMenu(authClient, session);
        var feedMenu = new FeedMenu(postClient, subredditClient, session);

        while(true){
            if (session.isLoggedIn()) {
                System.out.println("\n --- Insiders Main Menu ---");
                System.out.println("1. Browse Posts (Feed)");
                System.out.println("2. Logout");
                System.out.println("0. Exit");

                int choice = ConsoleIO.readInt("Enter your choice: ");
                switch (choice){
                    case 1 -> feedMenu.showMenu();
                    case 2 -> {
                        session.logout();
                        System.out.println("Logged out successfully!");
                    }
                    case 0 -> {
                        System.out.println("See you next time!");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again!");
                }
            } else {
                System.out.println("\n --- Welcome to Insiders ---");
                System.out.println("1. Authenticate");
                System.out.println("0. Exit");

                int choice = ConsoleIO.readInt("Enter your choice: ");
                switch (choice){
                    case 1 -> {
                        if (authMenu.showMenu()) {
                            System.out.println("Welcome! Redirecting to feed...");
                            feedMenu.showMenu();
                        }
                    }
                    case 0 -> {
                        System.out.println("See you next time!");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again!");
                }
            }
        }
    }
}