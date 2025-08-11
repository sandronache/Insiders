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

    public void showMenu(){
        while(true){
            System.out.println("\n--- Authentication Menu ---");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Back");

            int choice = ConsoleIO.readInt("Enter your choice:");
            switch(choice){
                case 1 -> register();
                case 2 -> login();
                case 0 -> {return;}
                default -> System.out.println("Invalid choice.Please try again!");
            }
        }
    }

    public void register(){
        String username = ConsoleIO.readLine("Username: ");
        String email = ConsoleIO.readLine("Email: ");
        String password = ConsoleIO.readPassword("Password: ");
        var response = client.register(new RegisterRequestDto(username,email,password));

        if(response.success){
            System.out.println("User "+username+" has been registered successfully!");
        }else{
            System.out.println("Error ("+response.status+"): "+response.message);
        }
    }

    public void login(){
        String email = ConsoleIO.readLine("Email: ");
        String password = ConsoleIO.readPassword("Password: ");
        var response = client.login(new LoginRequestDto(email,password));

        if(response.success){
            sessionManager.set(response.data.userId(), response.data.username());
            System.out.println("Hello "+ sessionManager.username()+"! You have been logged in successfully!");
        }else{
            System.out.println("Error ("+response.status+"): "+response.message);
        }
    }
}
