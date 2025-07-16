package app;

import logger.LoggerFacade;
import model.AppData;
import model.Post;
import service.AppDataService;
import service.ContentService;
import util.Helper;

import java.util.Scanner;
import java.util.TreeMap;

public class CLIInterface implements AppInterface {
    private ContentService contentService;
    private AppDataService appDataService;
    private AppData appData;
    private boolean isAppOn;
    private final Scanner input = new Scanner(System.in);

    public CLIInterface(ContentService contentService,
                        AppDataService appDataService) {
        this.contentService = contentService;
        this.appDataService = appDataService;
        appData = appDataService.createAppData();
        isAppOn = true;

        LoggerFacade.debug("CLIInterface initialized");
    }

    private void deleteCurrentUser() {
        LoggerFacade.info("User deletion initiated for: " + appData.getLoggedUser().getUsername());

        appDataService.deleteUser(appData);
        appDataService.logout(appData);
        // ++ erasing all posted content (posts, replies, comments)
    }

    private void registerPrompt() {
        LoggerFacade.debug("Registration process started");

        System.out.println("Enter username (or type 'exit' to return): ");
        String username = input.nextLine();

        if (username.equalsIgnoreCase("exit")) {
            System.out.println("Registration cancelled.");
            LoggerFacade.info("Registration cancelled by user");
            authenticationPrompt();
            return;
        }

        String email;
        boolean validEmail = false;
        do {
            System.out.println("Enter email (or type 'exit' to return): ");
            email = input.nextLine();

            if (email.equalsIgnoreCase("exit")) {
                System.out.println("Registration cancelled.");
                LoggerFacade.info("Registration cancelled by user");
                authenticationPrompt();
                return;
            }

            if (email.contains("@")) {
                validEmail = true;
            } else {
                System.out.println("Invalid email format. Email must contain '@'.");
                LoggerFacade.warning("Invalid email format entered: " + email);
            }
        } while (!validEmail);

        String password;
        boolean validPassword = false;
        do {
            System.out.println("Enter password (or type 'exit' to return): ");
            password = input.nextLine();

            if (password.equalsIgnoreCase("exit")) {
                System.out.println("Registration cancelled.");
                LoggerFacade.info("Registration cancelled by user");
                authenticationPrompt();
                return;
            }

            if (password.length() >= 6) {
                validPassword = true;
            } else {
                System.out.println("Password must be at least 6 characters long.");
                LoggerFacade.warning("Password too short: " + password.length() + " characters");
            }
        } while (!validPassword);

        boolean isRegistered = appDataService.register(
                appData,
                username,
                email,
                password
        );

        if (isRegistered) {
            System.out.println("You have successfully registered!");
            LoggerFacade.info("New user registered: " + username);
        } else {
            System.out.println("Username already exists!");
            LoggerFacade.warning("Registration failed - username already exists: " + username);
            authenticationPrompt();
        }
    }

    private void loginPrompt() {
        LoggerFacade.debug("Login process started");

        System.out.println("Enter username (or type 'exit' to return): ");
        String username = input.nextLine();

        if (username.equalsIgnoreCase("exit")) {
            LoggerFacade.info("Login cancelled by user");
            System.out.println("Login cancelled.");
            authenticationPrompt();
            return;
        }

        System.out.println("Enter password: (or type 'exit' to return): ");
        String password = input.nextLine();

        if (password.equalsIgnoreCase("exit")) {
            LoggerFacade.info("Login cancelled by user");
            System.out.println("Login cancelled.");
            authenticationPrompt();
            return;
        }

        boolean isLoggedIn = appDataService.login(
                appData,
                username,
                password
        );

        if (isLoggedIn) {
            System.out.println("You have successfully logged in!");
            LoggerFacade.info("User logged in: " + username);
        } else {
            System.out.println("Wrong username or password!");
            LoggerFacade.warning("Failed login attempt for username: " + username);
            authenticationPrompt();
        }
    }

    private void authenticationPrompt() {
        LoggerFacade.debug("Authentication menu displayed");

        boolean doneAuthentication = false;
        while(!doneAuthentication) {
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.println(">>>");
            String choice = input.nextLine();
            switch (choice) {
                case "1":
                    registerPrompt();
                    doneAuthentication = true;
                    break;
                case "2":
                    loginPrompt();
                    doneAuthentication = true;
                    break;
                case "3":
                    isAppOn = false;
                    doneAuthentication = true;
                    break;
                default:
                    System.out.println("Invalid choice");
                    LoggerFacade.warning("Invalid authentication choice selected");
                    break;
            }
            clearCLI();
        }
    }

    private void clearCLI() {
        for (int i = 0; i < 10; i++) {
            System.out.println(); // simulates terminal clear
        }
    }

    private void showFeed() {
        LoggerFacade.debug("Displaying feed with " + appData.getLoadedPosts().size() + " posts");
        System.out.println(appDataService.renderFeed(appData));
    }

    private void addNewPostPrompt() {
        LoggerFacade.info("New post creation initiated by user: " + appData.getLoggedUser().getUsername());

        System.out.println("Enter the content of the post");
        String content = input.nextLine();

        appDataService.addPost(appData, content);
    }

    private void deleteCurrentUserPrompt() {
        LoggerFacade.info("User deletion requested for: " + appData.getLoggedUser().getUsername());

        System.out.println("Are you sure you want to delete your account? Type 'yes' or 'no':");
        String confirmation = input.nextLine().trim().toLowerCase();

        if (confirmation.equals("yes")) {
            LoggerFacade.info("User deletion confirmed for: " + appData.getLoggedUser().getUsername());

            deleteCurrentUser();
            System.out.println("Deleted current user");
        } else {
            LoggerFacade.info("User deletion cancelled by: " + appData.getLoggedUser().getUsername());
            System.out.println("Account deletion cancelled");
        }
    }

    private void logoutPrompt() {
        LoggerFacade.info("Logout initiated by user: " + appData.getLoggedUser().getUsername());

        appDataService.logout(appData);
        System.out.println("You have been logged out");
    }

    private int enterPostPrompt() {
        TreeMap<Integer, Post> posts = appData.getLoadedPosts();
        if (posts.isEmpty()) {
            LoggerFacade.warning("User attempted to access posts when none are available");
            System.out.println("No post available");
            return -1;
        }
        System.out.println("Insert post id:");
        while (true) {
            int postNumber = Integer.parseInt(input.nextLine());
            if (posts.containsKey(postNumber)) {
                LoggerFacade.info("User selected post with ID: " + postNumber);
                return postNumber;
            }
            LoggerFacade.warning("User entered invalid post ID: " + postNumber);
            System.out.println("Invalid choice, try again");
        }
    }

    private void feedPrompt() {
        LoggerFacade.debug("Feed menu displayed");

        boolean onFeed = true;
        int postNumber;
        while(onFeed) {
            showFeed();
            System.out.println("1. Create a new post");
            System.out.println("2. Delete a post");
            System.out.println("3. Enter a post");
            System.out.println("4. Delete current user");
            System.out.println("5. Logout");
            System.out.println("6. Exit");
            System.out.println(">>>");
            String choice = input.nextLine();
            switch (choice) {
                case "1":
                    addNewPostPrompt();
                    break;
                case "2":
                    postNumber = enterPostPrompt();
                    if (postNumber != -1) {
                        LoggerFacade.info("User initiated post deletion for post ID: " + postNumber);

                        appDataService.deletePost(
                                appData,
                                postNumber
                        );
                    }
                    break;
                case "3":
                    postNumber = enterPostPrompt();
                    if (postNumber != -1) {
                        LoggerFacade.debug("User viewing post with ID: " + postNumber);

                        clearCLI();
                        postPrompt(postNumber);
                    }
                    break;
                case "4":
                    deleteCurrentUserPrompt();
                    onFeed = false;
                    break;
                case "5":
                    logoutPrompt();
                    onFeed = false;
                    break;
                case "6":
                    isAppOn = false;
                    onFeed = false;
                    break;
                default:
                    System.out.println("Invalid choice");
                    LoggerFacade.warning("Invalid feed menu choice selected");
                    break;
            }
            clearCLI();
        }
    }

    private void votePostPrompt(Post chosenPost) {
        LoggerFacade.debug("Vote post menu displayed for post by: " + chosenPost.getUsername());

        System.out.println("1. Upvote");
        System.out.println("2. Downvote");
        System.out.println(">>>");
        String choice = input.nextLine();
        switch (choice) {
            case "1":
                LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " upvoted a post by " + chosenPost.getUsername());

                contentService.addUpvotePost(
                        chosenPost,
                        appData.getLoggedUser().getUsername()
                );

                System.out.println("Vote added successfully!");
                break;
            case "2":
                LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " downvoted a post by " + chosenPost.getUsername());

                contentService.addDownvotePost(
                        chosenPost,
                        appData.getLoggedUser().getUsername()
                );

                System.out.println("Vote added successfully!");
                break;
            default:
                System.out.println("Invalid choice");
                LoggerFacade.warning("Invalid vote choice selected");
                break;
        }
    }

    private void addCommentPrompt(Post chosenPost) {
        LoggerFacade.debug("User " + appData.getLoggedUser().getUsername() + " adding comment to post by " + chosenPost.getUsername());

        System.out.println("Text..:");
        String content = input.nextLine();

        contentService.addComment(
                chosenPost,
                content,
                appData.getLoggedUser().getUsername()
        );

        LoggerFacade.info("Comment added by user: " + appData.getLoggedUser().getUsername());
        System.out.println("Comment added successfully!");
    }

    private void addReplyPrompt(Post chosenPost) {
        LoggerFacade.debug("User initiating reply to a comment");

        System.out.println("Insert comment id found between \"[]\":");
        while(true) {
            String id = input.nextLine();
            if (Helper.isCommentIdValid(id)) {
                // ++check if id also exists
                LoggerFacade.debug("Reply to comment ID: " + id);

                System.out.println("Text..:");
                String content = input.nextLine();

                contentService.addReply(
                        chosenPost,
                        id,
                        content,
                        appData.getLoggedUser().getUsername()
                );

                LoggerFacade.info("Reply added to comment ID: " + id + " by user: " + appData.getLoggedUser().getUsername());
                break;
            }
            LoggerFacade.warning("Invalid comment ID format entered: " + id);
            System.out.println("Invalid choice, try again");
        }
    }

    private void deleteCommentOrReplyPrompt(Post chosenPost) {
        LoggerFacade.debug("User initiating comment deletion");

        System.out.println("Insert comment or reply id found between \"[]\":");
        while(true) {
            String id = input.nextLine();
            if (Helper.isCommentIdValid(id)) {
                // ++check if id also exists
                LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " deleted comment with ID: " + id);

                contentService.deleteCommentOrReply(
                        chosenPost,
                        id
                );

                break;
            }
            LoggerFacade.warning("Invalid comment ID format entered: " + id);
            System.out.println("Invalid choice, try again");
        }
    }

    private void voteCommentOrReplyPrompt(Post chosenPost) {
        LoggerFacade.debug("User initiating comment voting");

        System.out.println("Insert comment or reply id found between \"[]\":");
        while(true) {
            String id = input.nextLine();
            if (Helper.isCommentIdValid(id)) {
                // ++check if id also exists
                LoggerFacade.debug("Valid comment ID entered: " + id);

                boolean isVoted = false;
                while(!isVoted) {
                    System.out.println("1. Upvote");
                    System.out.println("2. Downvote");
                    System.out.println(">>>");
                    String choice = input.nextLine();
                    switch (choice) {
                        case "1":
                            contentService.addUpvoteComment(
                                    chosenPost,
                                    id,
                                    appData.getLoggedUser().getUsername()
                            );

                            LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " upvoted comment ID: " + id);
                            isVoted = true;
                            break;
                        case "2":
                            contentService.addDownvoteComment(
                                    chosenPost,
                                    id,
                                    appData.getLoggedUser().getUsername()
                            );

                            LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " downvoted comment ID: " + id);
                            isVoted = true;
                            break;
                        default:
                            System.out.println("Invalid choice");
                            LoggerFacade.warning("Invalid vote choice selected");
                            break;
                    }
                }
                break;
            }
            LoggerFacade.warning("Invalid comment ID format entered: " + id);
            System.out.println("Invalid choice, try again");
        }
    }


    private void postPrompt(int id) {
        Post chosenPost = appData.getLoadedPosts().get(id);

        LoggerFacade.debug("User viewing post ID: " + id + " by " + chosenPost.getUsername());

        boolean isGoingBackToFeed = false;
        while (!isGoingBackToFeed) {
            System.out.println(contentService.renderFullPost(chosenPost));
            System.out.println("1. Vote post");
            System.out.println("2. Add comment");

            boolean hasComments = !chosenPost.getComments().isEmpty();
            if (hasComments) {
                System.out.println("3. Add reply");
            } else {
                System.out.println("3. Add reply (no comments available)");
            }

            System.out.println("4. Delete comment or reply");
            System.out.println("5. Vote comment or reply");
            System.out.println("6. Go back to feed");
            System.out.println(">>>");
            String choice = input.nextLine();
            switch (choice) {
                case "1":
                    votePostPrompt(chosenPost);
                    break;
                case "2":
                    addCommentPrompt(chosenPost);
                    break;
                case "3":
                    if (hasComments) {
                        addReplyPrompt(chosenPost);
                    } else {
                        System.out.println("No comments available to reply to. Add a comment first.");
                        LoggerFacade.warning("User attempted to add reply when no comments exist");
                    }
                    break;
                case "4":
                    deleteCommentOrReplyPrompt(chosenPost);
                    break;
                case "5":
                    voteCommentOrReplyPrompt(chosenPost);
                    break;
                case "6":
                    LoggerFacade.debug("User returning to feed from post view");
                    isGoingBackToFeed = true;
                    break;
                default:
                    System.out.println("Invalid choice");
                    LoggerFacade.warning("Invalid post menu choice selected");
                    break;
            }
            clearCLI();
        }
    }

    public void run(){
        LoggerFacade.info("Application interface started");
        while (true) {
            authenticationPrompt();
            if (!isAppOn) {
                appDataService.writeAppData(appData);
                break;
            }
            feedPrompt();
            if (!isAppOn) {
                appDataService.writeAppData(appData);
                break;
            }
        }
    }
}
