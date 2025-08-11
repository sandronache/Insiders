package com.insiders;

import com.insiders.clients.AuthClient;
import com.insiders.menu.AuthMenu;
import com.insiders.session.SessionManager;
import com.insiders.util.ConsoleIO;


public class Main {
    public static void main(String[] args) {
        String base = System.getenv().getOrDefault("API_BASE_URL", "http://localhost:8080");
        var session = new SessionManager();
        var authClient = new AuthClient(base, session::authHeaders);
        var authMenu = new AuthMenu(authClient, session);

        while(true){
            System.out.println("\n --- Welcome to Insiders ---");
            System.out.println("1. Authenticate");
            System.out.println("0. Exit");

            int choice = ConsoleIO.readInt("Enter your choice: ");
            switch (choice){
                case 1-> authMenu.showMenu();
                case 0 -> {
                    System.out.println("See you next time!");
                    return;
                }
                default -> System.out.println("Invalid choice.Please try again!");
            }
        }
    }
}