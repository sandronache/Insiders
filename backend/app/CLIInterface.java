package app;

import model.AppData;
import service.AppDataService;
import service.ContentService;
import utils.Helper;
import model.Post;

import java.util.LinkedList;
import java.util.Scanner;

public class CLIInterface implements AppInterface {
    private ContentService contentService;
    private AppDataService appDataService;
    private AppData appData;
    private final Scanner input = new Scanner(System.in);

    public CLIInterface(ContentService contentService,
                        AppDataService appDataService) {
        this.contentService = contentService;
        this.appDataService = appDataService;
        appData = appDataService.createAppData();
    }

    private void deleteCurrentUser() {
        appDataService.deleteUser(appData);
        appDataService.logout(appData);
        // ++ erasing all posted content (posts, replies, comments)
    }

    private void registerPrompt() {
        System.out.println("Enter username: ");
        String username = input.nextLine();
        System.out.println("Enter email: ");
        String email = input.nextLine();
        System.out.println("Enter password: ");
        String password = input.nextLine();

        boolean isRegistered = appDataService.register(
                appData,
                username,
                email,
                password
        );

        if (isRegistered) {
            System.out.println("You have successfully registered!");
        } else {
            System.out.println("Username already exists!");
            registerPrompt();
        }
    }

    private void loginPrompt() {
        System.out.println("Enter username: ");
        String username = input.nextLine();
        System.out.println("Enter password: ");
        String password = input.nextLine();

        boolean isLoggedIn = appDataService.login(
                appData,
                username,
                password
        );

        if (isLoggedIn) {
            System.out.println("You have successfully logged in!");
        } else {
            System.out.println("Wrong username or password!");
            loginPrompt();
        }
    }

    private void authenticationPrompt() {
        boolean doneAuthentication = false;
        while(!doneAuthentication) {
            System.out.println("1. Register");
            System.out.println("2. Login");
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
                default:
                    System.out.println("Invalid choice");
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
        System.out.println(appDataService.renderFeed(appData));
    }

    private void addNewPostPrompt() {
        System.out.println("Enter the content of the post");
        String content = input.nextLine();

        appDataService.addPost(appData, content);
    }

    private void deleteCurrentUserPrompt() {
        deleteCurrentUser();
        System.out.println("Deleted current user");
    }

    private void logoutPrompt() {
        appDataService.logout(appData);
        System.out.println("You have been logged out");
    }

    private int enterPostPrompt() {
        LinkedList<Post> posts = appData.getLoadedPosts();
        if (posts.isEmpty()) {
            System.out.println("No post available");
            return -1;
        }
        System.out.println("Insert post id (<" + posts.size() + "):");
        while (true) {
            int postNumber = Integer.parseInt(input.nextLine());
            if (postNumber >= 0 && postNumber < posts.size()) {
                return postNumber;
            }
            System.out.println("Invalid choice, try again");
        }
    }

    private void feedPrompt() {
        boolean onFeed = true;
        int postNumber;
        while(onFeed) {
            showFeed();
            System.out.println("1. Create a new post");
            System.out.println("2. Delete a post");
            System.out.println("3. Enter a post");
            System.out.println("4. Delete current user");
            System.out.println("5. Logout");
            System.out.println(">>>");
            String choice = input.nextLine();
            switch (choice) {
                case "1":
                    addNewPostPrompt();
                    break;
                case "2":
                    postNumber = enterPostPrompt();
                    if (postNumber != -1) {
                        appDataService.deletePost(
                                appData,
                                postNumber
                        );
                    }
                    break;
                case "3":
                    postNumber = enterPostPrompt();
                    if (postNumber != -1) {
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
                default:
                    System.out.println("Invalid choice");
                    break;
            }
            clearCLI();
        }
    }

    private void votePostPrompt(Post chosenPost) {
        System.out.println("1. Upvote");
        System.out.println("2. Downvote");
        System.out.println(">>>");
        String choice = input.nextLine();
        switch (choice) {
            case "1":
                contentService.addUpvotePost(
                        chosenPost,
                        appData.getLoggedUser().getUsername()
                );
                System.out.println("Vote added successfully!");
                break;
            case "2":
                contentService.addDownvotePost(
                        chosenPost,
                        appData.getLoggedUser().getUsername()
                );
                System.out.println("Vote added successfully!");
                break;
            default:
                System.out.println("Invalid choice");
                break;
        }
    }

    private void addCommentPrompt(Post chosenPost) {
        System.out.println("Text..:");
        String content = input.nextLine();
        contentService.addComment(
                chosenPost,
                content,
                appData.getLoggedUser().getUsername()
        );
        System.out.println("Comment added successfully!");
    }

    private void addReplyPrompt(Post chosenPost) {
        System.out.println("Insert comment id found between \"[]\":");
        while(true) {
            String id = input.nextLine();
            if (Helper.isCommentIdValid(id)) {
                // ++check if id also exists
                System.out.println("Text..:");
                String content = input.nextLine();
                contentService.addReply(
                        chosenPost,
                        id,
                        content,
                        appData.getLoggedUser().getUsername()
                );
                break;
            }
            System.out.println("Invalid choice, try again");
        }
    }

    private void deleteCommentOrReplyPrompt(Post chosenPost) {
        System.out.println("Insert comment or reply id found between \"[]\":");
        while(true) {
            String id = input.nextLine();
            if (Helper.isCommentIdValid(id)) {
                // ++check if id also exists
                contentService.deleteCommentOrReply(
                        chosenPost,
                        id
                );
                break;
            }
            System.out.println("Invalid choice, try again");
        }
    }

    private void voteCommentOrReplyPrompt(Post chosenPost) {
        System.out.println("Insert comment or reply id found between \"[]\":");
        while(true) {
            String id = input.nextLine();
            if (Helper.isCommentIdValid(id)) {
                // ++check if id also exists
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
                            isVoted = true;
                            break;
                        case "2":
                            contentService.addDownvoteComment(
                                    chosenPost,
                                    id,
                                    appData.getLoggedUser().getUsername()
                            );
                            isVoted = true;
                            break;
                        default:
                            System.out.println("Invalid choice");
                            break;
                    }
                }
                break;
            }
            System.out.println("Invalid choice, try again");
        }
    }


    private void postPrompt(int id) {
        Post chosenPost = appData.getLoadedPosts().get(id);
        boolean isGoingBackToFeed = false;
        while (!isGoingBackToFeed) {
            System.out.println(contentService.renderFullPost(chosenPost));
            System.out.println("1. Vote post");
            System.out.println("2. Add comment");
            System.out.println("3. Add reply");
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
                    addReplyPrompt(chosenPost);
                    break;
                case "4":
                    deleteCommentOrReplyPrompt(chosenPost);
                    break;
                case "5":
                    voteCommentOrReplyPrompt(chosenPost);
                    break;
                case "6":
                    isGoingBackToFeed = true;
                    break;
                default:
                    System.out.println("Invalid choice");
                    break;
            }
            clearCLI();
        }
    }

    public void run(){
        while (true) {
            authenticationPrompt();
            feedPrompt();
        }
    }
}
