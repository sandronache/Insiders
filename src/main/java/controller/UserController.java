package main.java.controller;

import main.java.entity.User;
import main.java.service.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserManagementService userManagementService;

    @Autowired
    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    // For demonstration; in production, use session or security context

//    @PostMapping("/register")
//    public String register(@RequestParam String username,
//                           @RequestParam String email,
//                           @RequestParam String password) {
//        boolean success = userManagementService.register(appDataService.getAppData(), username, email, password);
//        return success ? "Registration successful" : "Registration failed";
//    }
//
//    @PostMapping("/login")
//    public String login(@RequestParam String username,
//                        @RequestParam String password) {
//        boolean success = userManagementService.login(appDataService.getAppData(), username, password);
//        return success ? "Login successful" : "Login failed";
//    }
//
//    @PostMapping("/logout")
//    public String logout() {
//        userManagementService.logout(appDataService.getAppData());
//        return "Logged out";
//    }
//
//    @DeleteMapping("/delete")
//    public String deleteUser() {
//        if (appDataService.getAppData().getLoggedUser() == null) {
//            return "No user logged in";
//        }
//        userManagementService.deleteUser(appDataService.getAppData());
//        return "User deleted";
//    }
//
//    @GetMapping("/me")
//    public User getCurrentUser() {
//        return appDataService.getAppData().getLoggedUser();
//    }
}
