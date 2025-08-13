package com.insiders.menu;

import com.insiders.clients.PostClient;
import com.insiders.clients.SubredditClient;
import com.insiders.dto.post.PostResponseDto;
import com.insiders.dto.subreddit.SubredditCreateRequestDto;
import com.insiders.dto.subreddit.SubredditResponseDto;
import com.insiders.dto.subreddit.SubredditUpdateRequestDto;
import com.insiders.http.ApiResult;
import com.insiders.session.SessionManager;
import com.insiders.util.ConsoleIO;
import com.insiders.util.TimeUtils;
import com.insiders.util.MenuFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SubredditMenu {
    private final SubredditClient subredditClient;
    private final SessionManager sessionManager;
    private final PostMenu postMenu;
    private final Map<Integer, String> subredditNameMapping = new HashMap<>();
    private final Map<Integer, UUID> postIdMapping = new HashMap<>();

    public SubredditMenu(PostClient postClient, SubredditClient subredditClient, SessionManager sessionManager) {
        this.subredditClient = subredditClient;
        this.sessionManager = sessionManager;
        this.postMenu = new PostMenu(postClient, sessionManager);
    }

    public void showSubredditActions() {
        while (true) {
            MenuFormatter.printMenuHeader("Subreddit Actions");
            MenuFormatter.printMenuOptions(
                "1. Create Subreddit",
                "2. All Subreddits",
                "0. Back"
            );

            int choice = ConsoleIO.readInt("Choose option: ");

            switch (choice) {
                case 1 -> createSubreddit();
                case 2 -> allSubreddits();
                case 0 -> {
                    MenuFormatter.printInfoMessage("Returning to feed menu...");
                    return;
                }
                default -> MenuFormatter.printErrorMessage("Invalid choice!");
            }
        }
    }

    private void createSubreddit() {
        MenuFormatter.printMenuHeader("Create New Subreddit");

        String name = ConsoleIO.readLine("Enter subreddit name (lowercase, alphanumeric and underscore only): ");
        if (name.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Subreddit name cannot be empty!");
            return;
        }

        String displayName = ConsoleIO.readLine("Enter display name: ");
        if (displayName.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Display name cannot be empty!");
            return;
        }

        String description = ConsoleIO.readLine("Enter subreddit description: ");
        if (description.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Description cannot be empty!");
            return;
        }

        String iconUrl = ConsoleIO.readLine("Enter icon URL (optional, press Enter to skip): ");
        if (iconUrl.trim().isEmpty()) {
            iconUrl = null;
        }

        SubredditCreateRequestDto createRequest = new SubredditCreateRequestDto(
            name.toLowerCase().trim(),
            displayName.trim(),
            description.trim(),
            iconUrl
        );

        MenuFormatter.printInfoMessage("Creating subreddit...");
        ApiResult<SubredditResponseDto> result = subredditClient.createSubreddit(createRequest);

        if (result.success) {
            SubredditResponseDto createdSubreddit = result.data;
            MenuFormatter.printSuccessMessage("Subreddit created successfully!");
            MenuFormatter.printInfoMessage("Name: " + createdSubreddit.name());
            MenuFormatter.printInfoMessage("Display Name: " + createdSubreddit.displayName());
            MenuFormatter.printInfoMessage("Description: " + createdSubreddit.description());
            MenuFormatter.printInfoMessage("You can now create posts in this subreddit!");
        } else {
            MenuFormatter.printErrorMessage("Error creating subreddit: " + result.message + " (Status: " + result.status + ")");
        }
    }

    private void allSubreddits() {
        MenuFormatter.printMenuHeader("All Subreddits");
        MenuFormatter.printInfoMessage("Loading all subreddits...");

        ApiResult<List<SubredditResponseDto>> result = subredditClient.getAllSubreddits();

        if (result.success) {
            List<SubredditResponseDto> subreddits = result.data;

            if (subreddits.isEmpty()) {
                MenuFormatter.printInfoMessage("No subreddits found.");
                MenuFormatter.printInfoMessage("Be the first to create a subreddit!");
                return;
            } else {
                displaySubredditsList(subreddits);
                showSubredditManagementOptions();
            }
        } else {
            MenuFormatter.printErrorMessage("Error loading subreddits: " + result.message);
            return;
        }
    }

    private void displaySubredditsList(List<SubredditResponseDto> subreddits) {
        subredditNameMapping.clear();
        MenuFormatter.printMenuHeader("All Subreddits (" + subreddits.size() + ")");

        for (int i = 0; i < subreddits.size(); i++) {
            SubredditResponseDto subreddit = subreddits.get(i);
            int simpleId = i + 1;
            subredditNameMapping.put(simpleId, subreddit.name());

            displaySubredditCard(simpleId, subreddit);
        }
    }

    private void displaySubredditCard(int id, SubredditResponseDto subreddit) {
        int width = 80;
        String timeAgo = TimeUtils.getRelativeTime(subreddit.createdAt().toString());

        System.out.println(MenuFormatter.TOP_LEFT + MenuFormatter.HORIZONTAL_LINE.repeat(width - 2) + MenuFormatter.TOP_RIGHT);

        String idLine = String.format("ID: %s%d%s", MenuFormatter.YELLOW + MenuFormatter.BOLD, id, MenuFormatter.RESET);
        String nameLine = String.format("Name: %sr/%s%s", MenuFormatter.CYAN + MenuFormatter.BOLD, subreddit.name(), MenuFormatter.RESET);
        printSubredditLine(idLine, width);
        printSubredditLine(nameLine, width);

        String displayNameLine = String.format("Display Name: %s%s%s", MenuFormatter.GREEN + MenuFormatter.BOLD, subreddit.displayName(), MenuFormatter.RESET);
        printSubredditLine(displayNameLine, width);

        String descriptionLine = String.format("Description: %s%s%s", MenuFormatter.WHITE, subreddit.description(), MenuFormatter.RESET);
        printSubredditLine(descriptionLine, width);

        String membersLine = String.format("Members: %s%d%s", MenuFormatter.BLUE, subreddit.memberCount(), MenuFormatter.RESET);
        String postsLine = String.format("Posts: %s%d%s", MenuFormatter.PURPLE, subreddit.postCount(), MenuFormatter.RESET);
        printSubredditLine(membersLine, width);
        printSubredditLine(postsLine, width);

        String timeLine = String.format("Created: %s%s%s", MenuFormatter.PURPLE, timeAgo, MenuFormatter.RESET);
        printSubredditLine(timeLine, width);

        System.out.println(MenuFormatter.BOTTOM_LEFT + MenuFormatter.HORIZONTAL_LINE.repeat(width - 2) + MenuFormatter.BOTTOM_RIGHT);
        System.out.println();
    }

    private void printSubredditLine(String text, int width) {
        String cleanText = text.replaceAll("\u001B\\[[;\\d]*m", "");
        String padding = " ".repeat(Math.max(0, width - 4 - cleanText.length()));
        System.out.println(MenuFormatter.VERTICAL_LINE + " " + text + padding + " " + MenuFormatter.VERTICAL_LINE);
    }

    private void showSubredditManagementOptions() {
        while (true) {
            MenuFormatter.printMenuHeader("Subreddit Management");
            MenuFormatter.printMenuOptions(
                "1. View posts from subreddit",
                "2. Edit subreddit",
                "3. Delete subreddit",
                "0. Back to Subreddit Actions"
            );

            int choice = ConsoleIO.readInt("Choose option: ");
            switch (choice) {
                case 1 -> selectSubredditForPosts();
                case 2 -> editSubreddit();
                case 3 -> deleteSubreddit();
                case 0 -> {
                    MenuFormatter.printInfoMessage("Returning to subreddit actions...");
                    return;
                }
                default -> MenuFormatter.printErrorMessage("Invalid choice!");
            }
        }
    }

    private void selectSubredditForPosts() {
        String subredditIdStr = ConsoleIO.readLine("Enter the subreddit ID to view posts: ");
        try {
            int simpleId = Integer.parseInt(subredditIdStr);

            String subredditName = subredditNameMapping.get(simpleId);
            if (subredditName != null) {
                viewSubredditPosts(subredditName);
            } else {
                MenuFormatter.printErrorMessage("Invalid subreddit ID! Please choose a number from the list above.");
            }
        } catch (NumberFormatException e) {
            MenuFormatter.printErrorMessage("Please enter a valid number from the subreddits list.");
        }
    }

    private void viewSubredditPosts(String subredditName) {
        MenuFormatter.printMenuHeader("Posts in Subreddit: r/" + subredditName);
        MenuFormatter.printInfoMessage("Loading posts...");

        ApiResult<List<PostResponseDto>> result = subredditClient.getSubredditPosts(subredditName);

        if (result.success) {
            List<PostResponseDto> posts = result.data;
            if (posts.isEmpty()) {
                MenuFormatter.printInfoMessage("No posts found in subreddit 'r/" + subredditName + "'.");
                MenuFormatter.printInfoMessage("Be the first to create a post in this subreddit!");
            } else {
                MenuFormatter.printInfoMessage("Found " + posts.size() + " posts in r/" + subredditName);
                displayPostsAndNavigate(posts);
            }
        } else {
            MenuFormatter.printErrorMessage("Error loading posts from subreddit 'r/" + subredditName + "': " + result.message);
            MenuFormatter.printInfoMessage("Make sure the subreddit name is correct and exists.");
        }
    }

    private void displayPostsAndNavigate(List<PostResponseDto> posts) {
        if (posts.isEmpty()) {
            MenuFormatter.printInfoMessage("No posts found.");
            return;
        }

        postIdMapping.clear();

        for (int i = 0; i < posts.size(); i++) {
            PostResponseDto post = posts.get(i);
            int simpleId = i + 1;
            postIdMapping.put(simpleId, post.id());

            boolean isOwnPost = post.author().equals(sessionManager.username());
            int score = post.upvotes() - post.downvotes();
            String timeAgo = TimeUtils.getRelativeTime(post.createdAt().toString());

            MenuFormatter.printPostCard(
                simpleId,
                post.title(),
                post.author(),
                isOwnPost,
                post.subreddit(),
                score,
                timeAgo
            );
        }

        String input = ConsoleIO.readLine("Enter post ID to view/manage (or press Enter to continue): ");
        if (!input.trim().isEmpty()) {
            try {
                int simpleId = Integer.parseInt(input);
                UUID actualPostId = postIdMapping.get(simpleId);
                if (actualPostId != null) {
                    postMenu.showPostManagementMenu(actualPostId);
                } else {
                    MenuFormatter.printErrorMessage("Invalid post ID! Please choose a number from the list above.");
                }
            } catch (NumberFormatException e) {
                MenuFormatter.printErrorMessage("Please enter a valid number from the post list.");
            }
        }
    }

    private void editSubreddit() {
        String subredditName = ConsoleIO.readLine("Enter the subreddit name to edit: ");
        if (subredditName.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Subreddit name cannot be empty!");
            return;
        }

        ApiResult<SubredditResponseDto> subredditResult = subredditClient.getSubredditByName(subredditName.trim());
        if (!subredditResult.success) {
            MenuFormatter.printErrorMessage("Error loading subreddit 'r/" + subredditName + "': " + subredditResult.message);
            MenuFormatter.printInfoMessage("Make sure the subreddit name is correct and exists.");
            return;
        }

        SubredditResponseDto subreddit = subredditResult.data;

        MenuFormatter.printMenuHeader("Edit Subreddit: r/" + subreddit.name());
        MenuFormatter.printInfoMessage("Current Display Name: " + subreddit.displayName());
        MenuFormatter.printInfoMessage("Current Description: " + subreddit.description());

        String newDisplayName = ConsoleIO.readLine("Enter new display name (or press Enter to keep current): ");
        String newDescription = ConsoleIO.readLine("Enter new description (or press Enter to keep current): ");
        String newIconUrl = ConsoleIO.readLine("Enter new icon URL (or press Enter to keep current): ");

        SubredditUpdateRequestDto updateRequest = new SubredditUpdateRequestDto(
            newDisplayName.isEmpty() ? null : newDisplayName,
            newDescription.isEmpty() ? null : newDescription,
            newIconUrl.isEmpty() ? null : newIconUrl
        );

        MenuFormatter.printInfoMessage("Updating subreddit...");
        ApiResult<SubredditResponseDto> result = subredditClient.updateSubreddit(subredditName.trim(), updateRequest);

        if (result.success) {
            MenuFormatter.printSuccessMessage("Subreddit updated successfully!");
            MenuFormatter.printInfoMessage("Updated Display Name: " + result.data.displayName());
            MenuFormatter.printInfoMessage("Updated Description: " + result.data.description());
        } else {
            MenuFormatter.printErrorMessage("Error updating subreddit: " + result.message);
        }
    }

    private void deleteSubreddit() {
        String subredditName = ConsoleIO.readLine("Enter the subreddit name to delete: ");
        if (subredditName.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Subreddit name cannot be empty!");
            return;
        }

        ApiResult<SubredditResponseDto> subredditResult = subredditClient.getSubredditByName(subredditName.trim());
        if (!subredditResult.success) {
            MenuFormatter.printErrorMessage("Error loading subreddit 'r/" + subredditName + "': " + subredditResult.message);
            MenuFormatter.printInfoMessage("Make sure the subreddit name is correct and exists.");
            return;
        }

        SubredditResponseDto subreddit = subredditResult.data;

        MenuFormatter.printMenuHeader("Delete Subreddit");
        MenuFormatter.printInfoMessage("Subreddit: r/" + subreddit.name());
        MenuFormatter.printInfoMessage("Display Name: " + subreddit.displayName());
        MenuFormatter.printInfoMessage("Posts: " + subreddit.postCount());

        if (subreddit.postCount() > 0) {
            MenuFormatter.printErrorMessage("Cannot delete this subreddit because it contains " + subreddit.postCount() + " post(s).");
            MenuFormatter.printInfoMessage("You must delete all posts from this subreddit before you can delete it.");
            return;
        }

        MenuFormatter.printWarningMessage("Are you sure you want to delete this subreddit?");
        String confirm = ConsoleIO.readLine("Type 'yes' to confirm deletion: ");

        if ("yes".equalsIgnoreCase(confirm)) {
            ApiResult<String> result = subredditClient.deleteSubreddit(subredditName.trim());
            if (result.success) {
                MenuFormatter.printSuccessMessage("Subreddit deleted successfully!");
            } else {
                MenuFormatter.printErrorMessage("Error deleting subreddit: " + result.message);
            }
        } else {
            MenuFormatter.printInfoMessage("Delete cancelled.");
        }
    }
}
