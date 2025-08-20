package com.insiders.menu;

import com.insiders.clients.AuthClient;
import com.insiders.dto.auth.LoginRequestDto;
import com.insiders.dto.auth.RegisterRequestDto;
import com.insiders.session.SessionManager;
import com.insiders.util.ConsoleIO;
import com.insiders.util.EmailValidator;
import com.insiders.util.MenuFormatter;

public class AuthMenu {
    private final AuthClient client;
    private final SessionManager sessionManager;

    public AuthMenu(AuthClient client, SessionManager sessionManager) {
        this.client = client;
        this.sessionManager = sessionManager;
    }

    public boolean showMenu(){
        while(true){
            MenuFormatter.printMenuHeader("Authentication Menu");
            MenuFormatter.printMenuOptions(
                "1. Register",
                "2. Login",
                "0. Back"
            );

            int choice = ConsoleIO.readInt("Enter your choice:");
            switch(choice){
                case 1 -> {
                    if (register()) {
                        return true;
                    }
                }
                case 2 -> {
                    if (login()) {
                        return true;
                    }
                }
                case 0 -> {
                    return false;
                }
                default -> MenuFormatter.printErrorMessage("Invalid choice. Please try again!");
            }
        }
    }

    public boolean register(){
        MenuFormatter.printMenuHeader("User Registration");

        String username = ConsoleIO.readLine("Username: ");

        String email;
        while (true) {
            email = ConsoleIO.readLine("Email: ");
            if (EmailValidator.isValidEmail(email)) {
                break;
            } else {
                MenuFormatter.printErrorMessage(EmailValidator.getEmailErrorMessage(email));
                MenuFormatter.printInfoMessage("Please try again with a valid email address.");
            }
        }

        String password = MenuFormatter.readPasswordWithMasking("Password: ");

        MenuFormatter.printInfoMessage("Creating your account...");
        var response = client.register(new RegisterRequestDto(username, email, password));

        if(response.success){
            MenuFormatter.printSuccessMessage("User " + username + " has been registered successfully!");

            MenuFormatter.printInfoMessage("Attempting automatic login...");
            var loginResponse = client.login(new LoginRequestDto(email, password));
            if(loginResponse.success){
                sessionManager.set(loginResponse.data.userId(), loginResponse.data.username());
                MenuFormatter.printSuccessMessage("You have been automatically logged in!");
                MenuFormatter.printInfoMessage("Welcome to Insiders, " + username + "!");
                return true;
            } else {
                MenuFormatter.printWarningMessage("Registration successful but automatic login failed.");
                MenuFormatter.printInfoMessage("Please try logging in manually.");
            }
        } else {
            MenuFormatter.printErrorMessage("Registration failed: " + response.message + " (Status: " + response.status + ")");
        }
        return false;
    }

    public boolean login(){
        MenuFormatter.printMenuHeader("User Login");

        String email;
        while (true) {
            email = ConsoleIO.readLine("Email: ");
            if (EmailValidator.isValidEmail(email)) {
                break;
            } else {
                MenuFormatter.printErrorMessage(EmailValidator.getEmailErrorMessage(email));
                MenuFormatter.printInfoMessage("Please try again with a valid email address.");
            }
        }

        String password = MenuFormatter.readPasswordWithMasking("Password: ");

        MenuFormatter.printInfoMessage("Authenticating...");
        var response = client.login(new LoginRequestDto(email, password));

        if(response.success){
            sessionManager.set(response.data.userId(), response.data.username());
            MenuFormatter.printSuccessMessage("Hello " + sessionManager.username() + "! You have been logged in successfully!");
            MenuFormatter.printInfoMessage("Welcome back to Insiders!");
            return true;
        } else {
            switch (response.status) {
                case 401 -> {
                    MenuFormatter.printErrorMessage("Invalid email or password!");
                    MenuFormatter.printInfoMessage("Please check your credentials and try again.");
                    MenuFormatter.printInfoMessage("Tip: Make sure your email is correct and password is case-sensitive.");
                }
                case 404 -> {
                    MenuFormatter.printErrorMessage("User account not found!");
                    MenuFormatter.printInfoMessage("Please check if your email is correct or register a new account.");
                }
                case 403 -> {
                    MenuFormatter.printErrorMessage("Account access denied!");
                    MenuFormatter.printInfoMessage("Your account may be suspended or require verification.");
                }
                case 429 -> {
                    MenuFormatter.printErrorMessage("Too many login attempts!");
                    MenuFormatter.printInfoMessage("Please wait a few minutes before trying again.");
                }
                case 500, 502, 503 -> {
                    MenuFormatter.printErrorMessage("Server error occurred!");
                    MenuFormatter.printInfoMessage("Please try again later or contact support if the problem persists.");
                }
                default -> {
                    MenuFormatter.printErrorMessage("Login failed: " + response.message);
                    MenuFormatter.printInfoMessage("Status code: " + response.status);
                    if (response.message != null && !response.message.trim().isEmpty()) {
                        MenuFormatter.printInfoMessage("Details: " + response.message);
                    }
                }
            }
            return false;
        }
    }
}
