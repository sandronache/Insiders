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
            System.out.println("\n=== Subreddit Actions ===");
            System.out.println("1. Create Subreddit");
            System.out.println("2. All Subreddits");
            System.out.println("0. Back");

            int choice = ConsoleIO.readInt("Choose option: ");

            switch (choice) {
                case 1 -> createSubreddit();
                case 2 -> allSubreddits();
                case 0 -> {
                    System.out.println("Returning to feed menu...");
                    return;
                }
                default -> System.out.println("Invalid choice!");
            }
        }
    }

    private void createSubreddit() {
        System.out.println("\n=== Create New Subreddit ===");

        String name = ConsoleIO.readLine("Enter subreddit name (lowercase, alphanumeric and underscore only): ");
        if (name.trim().isEmpty()) {
            System.out.println("Subreddit name cannot be empty!");
            return;
        }

        String displayName = ConsoleIO.readLine("Enter display name: ");
        if (displayName.trim().isEmpty()) {
            System.out.println("Display name cannot be empty!");
            return;
        }

        String description = ConsoleIO.readLine("Enter subreddit description: ");
        if (description.trim().isEmpty()) {
            System.out.println("Description cannot be empty!");
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

        System.out.println("Creating subreddit...");
        ApiResult<SubredditResponseDto> result = subredditClient.createSubreddit(createRequest);

        if (result.success) {
            SubredditResponseDto createdSubreddit = result.data;
            System.out.println("✅ Subreddit created successfully!");
            System.out.println("Name: " + createdSubreddit.name());
            System.out.println("Display Name: " + createdSubreddit.displayName());
            System.out.println("Description: " + createdSubreddit.description());
            System.out.println("You can now create posts in this subreddit!");
        } else {
            System.out.println("❌ Error creating subreddit: " + result.message + " (Status: " + result.status + ")");
        }
    }

    private void allSubreddits() {
        System.out.println("\n=== All Subreddits ===");
        System.out.println("Loading all subreddits...");

        ApiResult<List<SubredditResponseDto>> result = subredditClient.getAllSubreddits();

        if (result.success) {
            List<SubredditResponseDto> subreddits = result.data;

            if (subreddits.isEmpty()) {
                System.out.println("No subreddits found.");
                System.out.println("Be the first to create a subreddit!");
                return;
            } else {
                displaySubredditsList(subreddits);
                showSubredditManagementOptions();
            }
        } else {
            System.out.println("Error loading subreddits: " + result.message);
            return;
        }
    }

    private void displaySubredditsList(List<SubredditResponseDto> subreddits) {
        subredditNameMapping.clear();
        System.out.println("\n--- All Subreddits (" + subreddits.size() + ") ---");

        for (int i = 0; i < subreddits.size(); i++) {
            SubredditResponseDto subreddit = subreddits.get(i);
            int simpleId = i + 1;
            subredditNameMapping.put(simpleId, subreddit.name());

            System.out.println("ID: " + simpleId);
            System.out.println("Name: " + subreddit.name());
            System.out.println("Display Name: " + subreddit.displayName());
            System.out.println("Description: " + subreddit.description());
            System.out.println("Members: " + subreddit.memberCount());
            System.out.println("Posts: " + subreddit.postCount());
            System.out.println("Created: " + TimeUtils.getRelativeTime(subreddit.createdAt().toString()));
            System.out.println("---");
        }
    }

    private void showSubredditManagementOptions() {
        while (true) {
            System.out.println("\n=== Subreddit Management ===");
            System.out.println("1. View posts from subreddit");
            System.out.println("2. Edit subreddit");
            System.out.println("3. Delete subreddit");
            System.out.println("0. Back to Subreddit Actions");

            int choice = ConsoleIO.readInt("Choose option: ");
            switch (choice) {
                case 1 -> selectSubredditForPosts();
                case 2 -> editSubreddit();
                case 3 -> deleteSubreddit();
                case 0 -> {
                    System.out.println("Returning to subreddit actions...");
                    return;
                }
                default -> System.out.println("Invalid choice!");
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
                System.out.println("Invalid subreddit ID! Please choose a number from the list above.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number from the subreddits list.");
        }
    }

    private void viewSubredditPosts(String subredditName) {
        System.out.println("\n=== Posts in Subreddit: " + subredditName + " ===");
        System.out.println("Loading posts...");

        ApiResult<List<PostResponseDto>> result = subredditClient.getSubredditPosts(subredditName);

        if (result.success) {
            List<PostResponseDto> posts = result.data;
            if (posts.isEmpty()) {
                System.out.println("No posts found in subreddit '" + subredditName + "'.");
                System.out.println("Be the first to create a post in this subreddit!");
            } else {
                System.out.println("Found " + posts.size() + " posts in r/" + subredditName);
                displayPostsAndNavigate(posts);
            }
        } else {
            System.out.println("❌ Error loading posts from subreddit '" + subredditName + "': " + result.message);
            System.out.println("Make sure the subreddit name is correct and exists.");
        }
    }

    private void displayPostsAndNavigate(List<PostResponseDto> posts) {
        if (posts.isEmpty()) {
            System.out.println("No posts found.");
            return;
        }

        postIdMapping.clear();

        System.out.println("\n--- Posts Feed ---");
        for (int i = 0; i < posts.size(); i++) {
            PostResponseDto post = posts.get(i);
            int simpleId = i + 1;

            postIdMapping.put(simpleId, post.id());

            System.out.println("ID: " + simpleId);
            System.out.println("Title: " + post.title());
            System.out.println("Author: " + post.author());

            if (post.author().equals(sessionManager.username())) {
                System.out.println("📝 [YOUR POST]");
            }

            System.out.println("Subreddit: " + post.subreddit());
            int score = post.upvotes() - post.downvotes();
            System.out.println("Score: " + score + " | Upvotes: " + post.upvotes() + " | Downvotes: " + post.downvotes());
            System.out.println("Posted: " + TimeUtils.getRelativeTime(post.createdAt().toString()));
            System.out.println("---");
        }

        String input = ConsoleIO.readLine("Enter post ID to view/manage (or press Enter to continue): ");
        if (!input.trim().isEmpty()) {
            try {
                int simpleId = Integer.parseInt(input);
                UUID actualPostId = postIdMapping.get(simpleId);
                if (actualPostId != null) {
                    postMenu.showPostManagementMenu(actualPostId);
                } else {
                    System.out.println("Invalid post ID! Please choose a number from the list above.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number from the post list.");
            }
        }
    }

    private void editSubreddit() {
        String subredditName = ConsoleIO.readLine("Enter the subreddit name to edit: ");
        if (subredditName.trim().isEmpty()) {
            System.out.println("Subreddit name cannot be empty!");
            return;
        }

        ApiResult<SubredditResponseDto> subredditResult = subredditClient.getSubredditByName(subredditName.trim());
        if (!subredditResult.success) {
            System.out.println("❌ Error loading subreddit '" + subredditName + "': " + subredditResult.message);
            System.out.println("Make sure the subreddit name is correct and exists.");
            return;
        }

        SubredditResponseDto subreddit = subredditResult.data;

        System.out.println("\n=== Edit Subreddit: " + subreddit.name() + " ===");
        System.out.println("Current Display Name: " + subreddit.displayName());
        System.out.println("Current Description: " + subreddit.description());

        String newDisplayName = ConsoleIO.readLine("Enter new display name (or press Enter to keep current): ");
        String newDescription = ConsoleIO.readLine("Enter new description (or press Enter to keep current): ");
        String newIconUrl = ConsoleIO.readLine("Enter new icon URL (or press Enter to keep current): ");

        SubredditUpdateRequestDto updateRequest = new SubredditUpdateRequestDto(
            newDisplayName.isEmpty() ? null : newDisplayName,
            newDescription.isEmpty() ? null : newDescription,
            newIconUrl.isEmpty() ? null : newIconUrl
        );

        System.out.println("Updating subreddit...");
        ApiResult<SubredditResponseDto> result = subredditClient.updateSubreddit(subredditName.trim(), updateRequest);

        if (result.success) {
            System.out.println("✅ Subreddit updated successfully!");
            System.out.println("Updated Display Name: " + result.data.displayName());
            System.out.println("Updated Description: " + result.data.description());
        } else {
            System.out.println("❌ Error updating subreddit: " + result.message);
        }
    }

    private void deleteSubreddit() {
        String subredditName = ConsoleIO.readLine("Enter the subreddit name to delete: ");
        if (subredditName.trim().isEmpty()) {
            System.out.println("Subreddit name cannot be empty!");
            return;
        }

        // Get subreddit details first
        ApiResult<SubredditResponseDto> subredditResult = subredditClient.getSubredditByName(subredditName.trim());
        if (!subredditResult.success) {
            System.out.println("❌ Error loading subreddit '" + subredditName + "': " + subredditResult.message);
            System.out.println("Make sure the subreddit name is correct and exists.");
            return;
        }

        SubredditResponseDto subreddit = subredditResult.data;

        System.out.println("\n=== Delete Subreddit ===");
        System.out.println("Subreddit: " + subreddit.name());
        System.out.println("Display Name: " + subreddit.displayName());
        System.out.println("Posts: " + subreddit.postCount());

        if (subreddit.postCount() > 0) {
            System.out.println("❌ Cannot delete this subreddit because it contains " + subreddit.postCount() + " post(s).");
            System.out.println("You must delete all posts from this subreddit before you can delete it.");
            return;
        }

        String confirm = ConsoleIO.readLine("Are you sure you want to delete this subreddit? (yes/no): ");

        if ("yes".equalsIgnoreCase(confirm)) {
            ApiResult<String> result = subredditClient.deleteSubreddit(subredditName.trim());
            if (result.success) {
                System.out.println("✅ Subreddit deleted successfully!");
            } else {
                System.out.println("❌ Error deleting subreddit: " + result.message);
            }
        } else {
            System.out.println("Delete cancelled.");
        }
    }
}
