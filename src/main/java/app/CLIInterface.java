/*
package main.java.app;

import main.java.logger.LoggerFacade;
import main.java.model.AppData;
import main.java.model.Comment;
import main.java.model.Post;
import main.java.service.AppDataService;
import main.java.service.ContentService;
import main.java.util.Helper;

import java.util.Scanner;
import java.util.TreeMap;

public class CLIInterface implements AppInterface {
    private ContentService contentService;
    private AppDataService appDataService;
    private boolean isAppOn;
    private final Scanner input = new Scanner(System.in);

    public CLIInterface(ContentService contentService,
                        AppDataService appDataService) {
        this.contentService = contentService;
        this.appDataService = appDataService;
        isAppOn = true;

        LoggerFacade.debug("CLIInterface initialized");
    }

    private void deleteCurrentUser() {
        LoggerFacade.info("User deletion initiated for: " + appDataService.getAppData().getUsername());

        appDataService.deleteUser(appData);
        appDataService.logout(appData);
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

        System.out.println("Enter the content of the post (or type 'exit' to return to feed):");
        String content = input.nextLine();

        if (content.trim().isEmpty()) {
            System.out.println("Post content cannot be empty.");
            LoggerFacade.warning("User attempted to create an empty post");
            return;
        }

        if (content.equalsIgnoreCase("exit")) {
            System.out.println("Post creation cancelled.");
            LoggerFacade.info("Post creation cancelled by user: " + appData.getLoggedUser().getUsername());
            return;
        }

        appDataService.addPost(appData, content);
    }

    private boolean deleteCurrentUserPrompt() {
        LoggerFacade.info("User deletion requested for: " + appData.getLoggedUser().getUsername());

        System.out.println("Are you sure you want to delete your account? Type 'yes' or 'no':");
        String confirmation = input.nextLine().trim().toLowerCase();

        if (confirmation.equals("yes")) {
            LoggerFacade.info("User deletion confirmed for: " + appData.getLoggedUser().getUsername());

            deleteCurrentUser();
            System.out.println("Deleted current user");
            return true;
        } else {
            LoggerFacade.info("User deletion cancelled by: " + appData.getLoggedUser().getUsername());
            System.out.println("Account deletion cancelled");
            return false;
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
        System.out.println("Insert post id (or type 'exit' to return to feed):");
        while (true) {
            String input = this.input.nextLine();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Operation cancelled.");
                LoggerFacade.info("Post operation cancelled by user: " + appData.getLoggedUser().getUsername());
                return -1;
            }

            try {
                int postNumber = Integer.parseInt(input);
                if (posts.containsKey(postNumber)) {
                    LoggerFacade.info("User selected post with ID: " + postNumber);
                    return postNumber;
                }
                LoggerFacade.warning("User entered invalid post ID: " + postNumber);
                System.out.println("Invalid choice, try again");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number, or 'exit' to return to feed");
                LoggerFacade.warning("User entered invalid input for post ID: " + input);
            }
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
                    boolean userDeleted = deleteCurrentUserPrompt();
                    if (userDeleted) {
                        onFeed = false;
                    }
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

    private void addCommentPrompt(Post chosenPost, int postId) {
        LoggerFacade.debug("User " + appData.getLoggedUser().getUsername() + " adding comment to post by " + chosenPost.getUsername());

        System.out.println("Text... (or type 'exit' to cancel):");
        String content = input.nextLine();

        if (content.equalsIgnoreCase("exit")) {
            LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " cancelled comment addition");
            System.out.println("Comment cancelled.");
            return;
        }

        if (content.trim().isEmpty()) {
            System.out.println("Comment cannot be empty.");
            LoggerFacade.warning("User attempted to add empty comment");
            return;
        }

        // Folosește AppDataService pentru a salva comentariul și în baza de date
        appDataService.addComment(appData, postId, content);

        System.out.println("Comment added successfully!");
        LoggerFacade.info("Comment added by user: " + appData.getLoggedUser().getUsername());
    }

    private void addReplyPrompt(Post chosenPost, int postId) {
        LoggerFacade.debug("User initiating reply to a comment");

        System.out.println("Insert comment id found between \"[]\" (or type 'exit' to return):");
        while(true) {
            String id = input.nextLine();

            if (id.equalsIgnoreCase("exit")) {
                LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " cancelled reply addition");
                System.out.println("Reply cancelled.");
                break;
            }

            if (Helper.isCommentIdValid(id)) {
                LoggerFacade.debug("Reply to comment ID: " + id);

                if (contentService.isCommentDeleted(chosenPost, id)) {
                    System.out.println("Comment is deleted, cannot add reply.");
                    LoggerFacade.warning("User attempted to add reply to deleted comment ID: " + id);
                    break;
                }

                System.out.println("Text... (or type 'exit' to cancel):");
                String content = input.nextLine();

                if (content.equalsIgnoreCase("exit")) {
                    LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " cancelled reply after selecting comment");
                    System.out.println("Reply cancelled.");
                    break;
                }

                if (content.trim().isEmpty()) {
                    System.out.println("Reply cannot be empty.");
                    LoggerFacade.warning("User attempted to add empty reply");
                    return;
                }

                // Extrage comentariul părinte din ID-ul ierarhic
                int commentId = Helper.extractFirstLevel(id);

                // Verifică dacă este un reply la un comentariu principal (nu la alt reply)
                if (Helper.extractRemainingLevels(id).isEmpty()) {
                    // Este un reply direct la comentariu - folosește AppDataService
                    appDataService.addReply(appData, postId, commentId, content);
                    System.out.println("Reply added successfully!");
                    LoggerFacade.info("Reply added to comment ID: " + id + " by user: " + appData.getLoggedUser().getUsername());
                } else {
                    // Este un reply la alt reply - folosește ContentService pentru logica complexă
                    boolean success = contentService.addReply(
                            chosenPost,
                            id,
                            content,
                            appData.getLoggedUser().getUsername()
                    );

                    if (success) {
                        System.out.println("Reply added successfully!");
                        LoggerFacade.info("Reply added to comment ID: " + id + " by user: " + appData.getLoggedUser().getUsername());
                    } else {
                        System.out.println("Failed to add reply. Comment may be deleted or invalid ID.");
                        LoggerFacade.warning("User attempted to add reply to deleted comment ID: " + id);
                    }
                }
                break;
            }
            LoggerFacade.warning("Invalid comment ID format entered: " + id);
            System.out.println("Invalid choice, try again");
        }
    }

    private void deleteCommentOrReplyPrompt(Post chosenPost) {
        LoggerFacade.debug("User initiating comment deletion");

        System.out.println("Insert comment or reply id found between \"[]\" (or type 'exit' to cancel):");
        while(true) {
            String id = input.nextLine();

            if (id.equalsIgnoreCase("exit")) {
                LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " cancelled comment insertion");
                System.out.println("Comment cancelled.");
                break;
            }

            if (Helper.isCommentIdValid(id)) {
                if (contentService.isCommentDeleted(chosenPost, id)) {
                    System.out.println("Comment is already deleted, cannot delete again.");
                    LoggerFacade.warning("User attempted to delete an already deleted comment ID: " + id);
                    break;
                }

                LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " deleted comment with ID: " + id);
                Comment target = Helper.findCommentById(chosenPost, id);
                String currentUser = appData.getLoggedUser().getUsername();

                if (target == null) {
                    System.out.println("Comentariul nu există.");
                    return;
                }
                if (!target.getUsername().equals(currentUser)) {
                    System.out.println("Nu poți șterge comentariul altui utilizator.");
                    return;
                }
                contentService.deleteCommentOrReply(
                        chosenPost,
                        id
                );

                System.out.println("Comment deleted successfully!");
                break;
            }
            LoggerFacade.warning("Invalid comment ID format entered: " + id);
            System.out.println("Invalid choice, try again");
        }
    }

    private void voteCommentOrReplyPrompt(Post chosenPost) {
        LoggerFacade.debug("User initiating comment voting");

        System.out.println("Insert comment or reply id found between \"[]\" (or type 'exit' to cancel):");
        while(true) {
            String id = input.nextLine();

            if (id.equalsIgnoreCase("exit")) {
                LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " cancelled comment voting");
                System.out.println("Voting cancelled.");
                break;
            }

            if (Helper.isCommentIdValid(id)) {
                LoggerFacade.debug("Valid comment ID entered: " + id);

                if (contentService.isCommentDeleted(chosenPost, id)) {
                    System.out.println("Comment is deleted, cannot vote on it.");
                    LoggerFacade.warning("User " + appData.getLoggedUser().getUsername() + " attempted to vote on a deleted comment ID: " + id);
                    break;
                }

                boolean isVoted = false;
                while(!isVoted) {
                    System.out.println("1. Upvote");
                    System.out.println("2. Downvote");
                    System.out.println("3. Exit");
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
                            System.out.println("Upvote added successfully!");
                            isVoted = true;
                            break;
                        case "2":
                            contentService.addDownvoteComment(
                                    chosenPost,
                                    id,
                                    appData.getLoggedUser().getUsername()
                            );

                            LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " downvoted comment ID: " + id);
                            System.out.println("Downvote added successfully!");
                            isVoted = true;
                            break;
                        case "3":
                            LoggerFacade.info("User " + appData.getLoggedUser().getUsername() + " exited comment voting for ID: " + id);
                            System.out.println("Exiting vote menu.");
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

            if (hasComments) {
                System.out.println("4. Delete comment or reply");
                System.out.println("5. Vote comment or reply");
            } else {
                System.out.println("4. Delete comment or reply (no comments available)");
                System.out.println("5. Vote comment or reply (no comments available)");
            }

            System.out.println("6. Go back to feed");
            System.out.println(">>>");
            String choice = input.nextLine();
            switch (choice) {
                case "1":
                    votePostPrompt(chosenPost);
                    break;
                case "2":
                    addCommentPrompt(chosenPost, id);
                    break;
                case "3":
                    if (hasComments) {
                        addReplyPrompt(chosenPost, id);
                    } else {
                        System.out.println("No comments available to reply to. Add a comment first.");
                        LoggerFacade.warning("User attempted to add reply when no comments exist");
                    }
                    break;
                case "4":
                    if (hasComments) {
                        deleteCommentOrReplyPrompt(chosenPost);
                    } else {
                        System.out.println("No comments available to delete. Add a comment first.");
                        LoggerFacade.warning("User attempted to delete comment when no comments exist");
                    }
                    break;
                case "5":
                    if (hasComments) {
                        voteCommentOrReplyPrompt(chosenPost);
                    } else {
                        System.out.println("No comments available to vote on. Add a comment first.");
                        LoggerFacade.warning("User attempted to vote on comment when no comments exist");
                    }
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
*/
