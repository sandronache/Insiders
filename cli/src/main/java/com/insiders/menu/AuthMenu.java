package com.insiders.menu;

import com.insiders.clients.AuthClient;
import com.insiders.dto.auth.LoginRequestDto;
import com.insiders.dto.auth.RegisterRequestDto;
import com.insiders.session.SessionManager;
import com.insiders.util.ConsoleIO;

public class AuthMenu {
    private final AuthClient client;
    private final SessionManager sessionManager;

    public AuthMenu(AuthClient client, SessionManager sessionManager) {
        this.client = client;
        this.sessionManager = sessionManager;
    }

    public boolean showMenu(){
        while(true){
            System.out.println("\n--- Authentication Menu ---");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Back");

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
                case 0 -> {return false;}
                default -> System.out.println("Invalid choice.Please try again!");
            }
        }
    }

    public boolean register(){
        String username = ConsoleIO.readLine("Username: ");
        String email = ConsoleIO.readLine("Email: ");
        String password = ConsoleIO.readPassword("Password: ");
        var response = client.register(new RegisterRequestDto(username,email,password));

        if(response.success){
            System.out.println("User "+username+" has been registered successfully!");
            var loginResponse = client.login(new LoginRequestDto(email, password));
            if(loginResponse.success){
                sessionManager.set(loginResponse.data.userId(), loginResponse.data.username());
                System.out.println("You have been automatically logged in!");
                return true;
            }
        } else {
            System.out.println("Error ("+response.status+"): "+response.message);
        }
        return false;
    }

    public boolean login(){
        String email = ConsoleIO.readLine("Email: ");
        String password = ConsoleIO.readPassword("Password: ");
        var response = client.login(new LoginRequestDto(email,password));

        if(response.success){
            sessionManager.set(response.data.userId(), response.data.username());
            System.out.println("Hello "+ sessionManager.username()+"! You have been logged in successfully!");
            return true;
        }else{
            System.out.println("Error ("+response.status+"): "+response.message);
            return false;
        }
    }
}
