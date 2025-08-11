package com.insiders.session;

import java.util.Map;
import java.util.UUID;

public class SessionManager {
    private UUID userId;
    private String username;
    private String token;

    public void set(UUID id, String user){
        this.userId = id;
        this.username = user;
    }

    public boolean isLoggedIn(){
        return userId != null;
    }

    public void logout(){
        userId=null;
        username=null;
        token=null;
    }

    public Map<String,String> authHeaders(){
        return token==null ? Map.of() : Map.of("Authorization","Bearer "+token);
    }

    public String username(){
        return username;
    }
}
