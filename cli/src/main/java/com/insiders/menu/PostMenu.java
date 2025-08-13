package com.insiders.menu;

import com.insiders.clients.PostClient;
import com.insiders.dto.comment.CommentCreateRequestDto;
import com.insiders.dto.comment.CommentResponseDto;
import com.insiders.dto.comment.CommentUpdateRequestDto;
import com.insiders.dto.post.PostResponseDto;
import com.insiders.dto.post.PostUpdateRequestDto;
import com.insiders.dto.vote.VoteResponseDto;
import com.insiders.http.ApiResult;
import com.insiders.session.SessionManager;
import com.insiders.util.ConsoleIO;
import com.insiders.util.TimeUtils;
import com.insiders.util.MenuFormatter;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PostMenu class handles individual post management and comment operations
 * Provides functionality for viewing, editing, deleting posts and managing comments
 */
public class PostMenu {
    private final PostClient client;
    private final SessionManager sessionManager;
    // Maps simple integer IDs to actual UUID comment IDs for user-friendly navigation
    private java.util.Map<Integer, UUID> commentIdMapping = new java.util.HashMap<>();

    /**
     * Constructor for PostMenu
     * @param client Client for post and comment-related API operations
     * @param sessionManager Manages user session information
     */
    public PostMenu(PostClient client, SessionManager sessionManager) {
        this.client = client;
        this.sessionManager = sessionManager;
    }

    /**
     * Displays the post management menu with all available actions
     * @param postId UUID of the post to manage
     */
    public void showPostManagementMenu(UUID postId) {
        MenuFormatter.printMenuHeader("Post Details");
        viewPostDetails(postId);

        MenuFormatter.printMenuHeader("Comments");
        viewComments(postId);

        while (true) {
            MenuFormatter.printMenuHeader("Post Management");
            MenuFormatter.printMenuOptions(
                "1. Refresh Post",
                "2. Edit Post",
                "3. Delete Post",
                "4. Upvote Post",
                "5. Downvote Post",
                "6. Comment Actions",
                "0. Back"
            );

            int choice = ConsoleIO.readInt("Enter your choice:");
            switch (choice) {
                case 1 -> {
                    MenuFormatter.printMenuHeader("Post Details");
                    viewPostDetails(postId);
                    MenuFormatter.printMenuHeader("Comments");
                    viewComments(postId);
                }
                case 2 -> editPost(postId);
                case 3 -> {
                    if (deletePost(postId)) {
                        return;
                    }
                }
                case 4 -> upvotePost(postId);
                case 5 -> downvotePost(postId);
                case 6 -> commentActions(postId);
                case 0 -> {
                    return;
                }
                default -> MenuFormatter.printErrorMessage("Invalid choice. Please try again!");
            }
        }
    }

    /**
     * Fetches and displays detailed information about a specific post
     * @param postId UUID of the post to display
     */
    private void viewPostDetails(UUID postId) {
        ApiResult<PostResponseDto> result = client.getPostById(postId);
        if (result.success) {
            PostResponseDto post = result.data;
            boolean isOwnPost = post.author().equals(sessionManager.username());
            String timeAgo = TimeUtils.getRelativeTime(post.createdAt().toString());

            MenuFormatter.printPostDetails(
                post.title(),
                post.content(),
                post.author(),
                isOwnPost,
                post.subreddit(),
                post.upvotes(),
                post.downvotes(),
                post.userVote(),
                timeAgo
            );
        } else {
            MenuFormatter.printErrorMessage("Error loading post: " + result.message);
        }
    }

    /**
     * Handles post editing functionality with ownership validation
     * @param postId UUID of the post to edit
     */
    private void editPost(UUID postId) {
        ApiResult<PostResponseDto> postResult = client.getPostById(postId);
        if (!postResult.success) {
            MenuFormatter.printErrorMessage("Error loading post: " + postResult.message);
            return;
        }

        PostResponseDto post = postResult.data;
        String currentUser = sessionManager.username();

        // Validate ownership
        if (!post.author().equals(currentUser)) {
            MenuFormatter.printErrorMessage("You can only edit your own posts!");
            MenuFormatter.printInfoMessage("This post belongs to: " + post.author());
            return;
        }

        MenuFormatter.printMenuHeader("Edit Post");
        String title = ConsoleIO.readLine("Enter new title (or press Enter to keep current): ");
        String content = ConsoleIO.readLine("Enter new content (or press Enter to keep current): ");

        PostUpdateRequestDto updateRequest = new PostUpdateRequestDto(
            title.isEmpty() ? null : title,
            content.isEmpty() ? null : content
        );

        ApiResult<PostResponseDto> result = client.updatePost(postId, updateRequest);
        if (result.success) {
            MenuFormatter.printSuccessMessage("Post updated successfully!");
        } else {
            MenuFormatter.printErrorMessage("Error updating post: " + result.message);
        }
    }

    /**
     * Handles post deletion with ownership validation and confirmation
     * @param postId UUID of the post to delete
     * @return true if post was deleted (should return to previous menu), false otherwise
     */
    private boolean deletePost(UUID postId) {
        ApiResult<PostResponseDto> postResult = client.getPostById(postId);
        if (!postResult.success) {
            MenuFormatter.printErrorMessage("Error loading post: " + postResult.message);
            return false;
        }

        PostResponseDto post = postResult.data;
        String currentUser = sessionManager.username();

        // Validate ownership
        if (!post.author().equals(currentUser)) {
            MenuFormatter.printErrorMessage("You can only delete your own posts!");
            MenuFormatter.printInfoMessage("This post belongs to: " + post.author());
            return false;
        }

        MenuFormatter.printWarningMessage("Are you sure you want to delete this post?");
        String confirm = ConsoleIO.readLine("Type 'yes' to confirm deletion: ");
        if ("yes".equalsIgnoreCase(confirm)) {
            ApiResult<String> result = client.deletePost(postId);
            if (result.success) {
                MenuFormatter.printSuccessMessage("Post deleted successfully!");
                return true;
            } else {
                MenuFormatter.printErrorMessage("Error deleting post: " + result.message);
            }
        } else {
            MenuFormatter.printInfoMessage("Deletion cancelled.");
        }
        return false;
    }

    /**
     * Handles upvoting a post, including toggle functionality for removing existing upvotes
     * @param postId UUID of the post to upvote
     */
    private void upvotePost(UUID postId) {
        MenuFormatter.printInfoMessage("Processing upvote...");

        // Check current vote state
        ApiResult<PostResponseDto> postResult = client.getPostById(postId);
        if (!postResult.success) {
            MenuFormatter.printErrorMessage("Error checking post state: " + postResult.message);
            return;
        }

        String currentVote = postResult.data.userVote();
        String voteType = "up";

        // Toggle logic for existing upvote
        if ("up".equals(currentVote)) {
            voteType = "none";
            MenuFormatter.printInfoMessage("Removing your upvote...");
        } else {
            MenuFormatter.printInfoMessage("Upvoting post...");
        }

        ApiResult<VoteResponseDto> result = client.votePost(postId, voteType);

        if (result.success) {
            VoteResponseDto vote = result.data;
            if (vote != null) {
                if ("none".equals(voteType)) {
                    MenuFormatter.printSuccessMessage("Upvote removed successfully!");
                } else {
                    MenuFormatter.printSuccessMessage("Post upvoted successfully!");
                }
                
                // Display updated score with colors
                String scoreInfo = String.format("Score: %s%d%s↑ %s%d%s↓",
                    MenuFormatter.GREEN, vote.upvotes(), MenuFormatter.RESET,
                    MenuFormatter.RED, vote.downvotes(), MenuFormatter.RESET);
                MenuFormatter.printInfoMessage(scoreInfo);

                // Display current vote status
                if (vote.userVote() != null && !vote.userVote().isEmpty() && !"none".equals(vote.userVote())) {
                    String voteDisplay = vote.userVote().equals("up") ? "⬆️ YOU UPVOTED" : "⬇️ YOU DOWNVOTED";
                    MenuFormatter.printInfoMessage("Your vote: " + voteDisplay);
                } else {
                    MenuFormatter.printInfoMessage("Your vote: None");
                }
            } else {
                MenuFormatter.printSuccessMessage("Vote successful but no vote data received.");
            }
        } else {
            MenuFormatter.printErrorMessage("Error voting: " + result.message + " (Status: " + result.status + ")");
        }
    }

    /**
     * Handles downvoting a post, including toggle functionality for removing existing downvotes
     * @param postId UUID of the post to downvote
     */
    private void downvotePost(UUID postId) {
        MenuFormatter.printInfoMessage("Processing downvote...");

        // Check current vote state
        ApiResult<PostResponseDto> postResult = client.getPostById(postId);
        if (!postResult.success) {
            MenuFormatter.printErrorMessage("Error checking post state: " + postResult.message);
            return;
        }

        String currentVote = postResult.data.userVote();
        String voteType = "down";

        // Toggle logic for existing downvote
        if ("down".equals(currentVote)) {
            voteType = "none";
            MenuFormatter.printInfoMessage("Removing your downvote...");
        } else {
            MenuFormatter.printInfoMessage("Downvoting post...");
        }

        ApiResult<VoteResponseDto> result = client.votePost(postId, voteType);

        if (result.success) {
            VoteResponseDto vote = result.data;
            if (vote != null) {
                if ("none".equals(voteType)) {
                    MenuFormatter.printSuccessMessage("Downvote removed successfully!");
                } else {
                    MenuFormatter.printSuccessMessage("Post downvoted successfully!");
                }
                
                // Display updated score with colors
                String scoreInfo = String.format("Score: %s%d%s↑ %s%d%s↓",
                    MenuFormatter.GREEN, vote.upvotes(), MenuFormatter.RESET,
                    MenuFormatter.RED, vote.downvotes(), MenuFormatter.RESET);
                MenuFormatter.printInfoMessage(scoreInfo);

                // Display current vote status
                if (vote.userVote() != null && !vote.userVote().isEmpty() && !"none".equals(vote.userVote())) {
                    String voteDisplay = vote.userVote().equals("up") ? "⬆️ YOU UPVOTED" : "⬇️ YOU DOWNVOTED";
                    MenuFormatter.printInfoMessage("Your vote: " + voteDisplay);
                } else {
                    MenuFormatter.printInfoMessage("Your vote: None");
                }
            } else {
                MenuFormatter.printSuccessMessage("Vote successful but no vote data received.");
            }
        } else {
            MenuFormatter.printErrorMessage("Error voting: " + result.message + " (Status: " + result.status + ")");
        }
    }

    /**
     * Fetches and displays all comments for a post in hierarchical format
     * @param postId UUID of the post to load comments for
     */
    private void viewComments(UUID postId) {
        MenuFormatter.printInfoMessage("Loading all comments for this post...");

        ApiResult<List<CommentResponseDto>> result = client.getCommentsForPost(postId);

        if (result.success) {
            List<CommentResponseDto> comments = result.data;
            if (comments == null) {
                MenuFormatter.printInfoMessage("No comments data received.");
            } else if (comments.isEmpty()) {
                MenuFormatter.printCommentsHeader(0);
            } else {
                // Clear previous comment mappings and create new ones
                commentIdMapping.clear();
                MenuFormatter.printCommentsHeader(comments.size());
                displayCommentsHierarchy(comments, 0, new AtomicInteger(1));
            }
        } else {
            MenuFormatter.printErrorMessage("Error loading comments: " + result.message + " (Status: " + result.status + ")");
        }
    }

    /**
     * Recursively displays comments in a hierarchical structure with proper indentation
     * @param comments List of comments to display
     * @param indentLevel Current indentation level for nested replies
     * @param simpleIdCounter Counter for generating simple integer IDs
     */
    private void displayCommentsHierarchy(List<CommentResponseDto> comments, int indentLevel, AtomicInteger simpleIdCounter) {
        for (CommentResponseDto comment : comments) {
            int simpleId = simpleIdCounter.getAndIncrement();
            commentIdMapping.put(simpleId, comment.id());

            displaySingleComment(comment, indentLevel, simpleId);

            // Recursively display replies with increased indentation
            if (comment.replies() != null && !comment.replies().isEmpty()) {
                displayCommentsHierarchy(comment.replies(), indentLevel + 1, simpleIdCounter);
            }
        }
    }

    /**
     * Displays a single comment with proper formatting and user indicators using MenuFormatter
     * @param comment The comment to display
     * @param indentLevel Indentation level for nested comments
     * @param simpleId Simple integer ID for user reference
     */
    private void displaySingleComment(CommentResponseDto comment, int indentLevel, int simpleId) {
        boolean isOwnComment = comment.author().equals(sessionManager.username());
        String timeAgo = TimeUtils.getRelativeTime(comment.createdAt().toString());

        MenuFormatter.printCommentCard(
            simpleId,
            comment.author(),
            isOwnComment,
            comment.content(),
            comment.score(),
            comment.upvotes(),
            comment.downvotes(),
            comment.userVote(),
            timeAgo,
            indentLevel
        );
    }

    /**
     * Displays comment management menu and handles user choices
     * @param postId UUID of the post containing the comments
     */
    private void commentActions(UUID postId) {
        MenuFormatter.printCommentActionsMenu();

        int choice = ConsoleIO.readInt("Choose option: ");

        switch (choice) {
            case 1 -> createTopLevelComment(postId);
            case 2 -> createReplyComment(postId);
            case 3 -> editComment(postId);
            case 4 -> deleteComment(postId);
            case 5 -> voteOnComment(postId, "UPVOTE");
            case 6 -> voteOnComment(postId, "DOWNVOTE");
            case 0 -> MenuFormatter.printInfoMessage("Returning to post management...");
            default -> MenuFormatter.printErrorMessage("Invalid choice!");
        }
    }

    /**
     * Creates a new top-level comment on the post
     * @param postId UUID of the post to comment on
     */
    private void createTopLevelComment(UUID postId) {
        String content = ConsoleIO.readLine("Enter your comment: ");
        if (content.trim().isEmpty()) {
            MenuFormatter.printErrorMessage("Comment cannot be empty!");
            return;
        }

        String author = sessionManager.username();
        CommentCreateRequestDto commentRequest = new CommentCreateRequestDto(content, author, null);

        ApiResult<CommentResponseDto> result = client.createComment(postId, commentRequest);
        if (result.success) {
            MenuFormatter.printSuccessMessage("Comment added successfully!");
            refreshCommentsDisplay(postId);
        } else {
            MenuFormatter.printErrorMessage("Error adding comment: " + result.message + " (Status: " + result.status + ")");
        }
    }

    /**
     * Creates a reply to an existing comment
     * @param postId UUID of the post containing the parent comment
     */
    private void createReplyComment(UUID postId) {
        String commentIdStr = ConsoleIO.readLine("Enter the comment ID you want to reply to: ");
        try {
            int simpleCommentId = Integer.parseInt(commentIdStr);

            UUID parentCommentId = commentIdMapping.get(simpleCommentId);
            if (parentCommentId == null) {
                MenuFormatter.printErrorMessage("Invalid comment ID! Please choose a number from the comments list above.");
                return;
            }

            String content = ConsoleIO.readLine("Enter your reply: ");
            if (content.trim().isEmpty()) {
                MenuFormatter.printErrorMessage("Reply cannot be empty!");
                return;
            }

            String author = sessionManager.username();
            CommentCreateRequestDto commentRequest = new CommentCreateRequestDto(content, author, parentCommentId);

            ApiResult<CommentResponseDto> result = client.createComment(postId, commentRequest);
            if (result.success) {
                MenuFormatter.printSuccessMessage("Reply added successfully!");
                refreshCommentsDisplay(postId);
            } else {
                MenuFormatter.printErrorMessage("Error adding reply: " + result.message + " (Status: " + result.status + ")");
            }
        } catch (NumberFormatException e) {
            MenuFormatter.printErrorMessage("Please enter a valid number from the comments list.");
        }
    }

    /**
     * Refreshes the comments display after modifications
     * @param postId UUID of the post to refresh comments for
     */
    private void refreshCommentsDisplay(UUID postId) {
        MenuFormatter.printMenuHeader("Updated Comments");
        viewComments(postId);
    }

    /**
     * Handles voting on comments with toggle functionality
     * @param postId UUID of the post containing the comment
     * @param voteType Type of vote ("UPVOTE" or "DOWNVOTE")
     */
    private void voteOnComment(UUID postId, String voteType) {
        String commentIdStr = ConsoleIO.readLine("Enter the comment ID you want to " +
            (voteType.equals("UPVOTE") ? "upvote" : "downvote") + ": ");
        try {
            int simpleCommentId = Integer.parseInt(commentIdStr);

            UUID commentId = commentIdMapping.get(simpleCommentId);
            if (commentId == null) {
                MenuFormatter.printErrorMessage("Invalid comment ID! Please choose a number from the comments list above.");
                return;
            }

            // Get current vote state
            ApiResult<List<CommentResponseDto>> commentsResult = client.getCommentsForPost(postId);
            String currentVote = null;

            if (commentsResult.success && commentsResult.data != null) {
                CommentResponseDto targetComment = findCommentInHierarchy(commentsResult.data, commentId);
                if (targetComment != null) {
                    currentVote = targetComment.userVote();
                }
            }

            String actualVoteType = voteType.equals("UPVOTE") ? "up" : "down";

            // Toggle logic for existing votes
            if (actualVoteType.equals(currentVote)) {
                actualVoteType = "none";
                MenuFormatter.printInfoMessage("Removing your " + (voteType.equals("UPVOTE") ? "upvote" : "downvote") + "...");
            } else {
                MenuFormatter.printInfoMessage((voteType.equals("UPVOTE") ? "Upvoting" : "Downvoting") + " comment...");
            }

            ApiResult<VoteResponseDto> result = client.voteComment(commentId, actualVoteType);

            if (result.success) {
                VoteResponseDto vote = result.data;
                if (vote != null) {
                    if ("none".equals(actualVoteType)) {
                        MenuFormatter.printSuccessMessage((voteType.equals("UPVOTE") ? "Upvote" : "Downvote") + " removed successfully!");
                    } else {
                        MenuFormatter.printSuccessMessage("Comment " + (voteType.equals("UPVOTE") ? "upvoted" : "downvoted") + " successfully!");
                    }

                    MenuFormatter.printInfoMessage("Score: " + vote.upvotes() + "↑ " + vote.downvotes() + "↓");

                    // Display current vote status
                    if (vote.userVote() != null && !vote.userVote().isEmpty() && !"none".equals(vote.userVote())) {
                        String voteDisplay = vote.userVote().equals("up") ? "⬆️ YOU UPVOTED" :
                                           vote.userVote().equals("down") ? "⬇️ YOU DOWNVOTED" : "";
                        if (!voteDisplay.isEmpty()) {
                            MenuFormatter.printInfoMessage("Your vote: " + voteDisplay);
                        }
                    } else {
                        MenuFormatter.printInfoMessage("Your vote: None");
                    }
                } else {
                    MenuFormatter.printSuccessMessage("Vote successful but no vote data received.");
                }
                refreshCommentsDisplay(postId);
            } else {
                MenuFormatter.printErrorMessage("Error voting on comment: " + result.message + " (Status: " + result.status + ")");
            }
        } catch (NumberFormatException e) {
            MenuFormatter.printErrorMessage("Please enter a valid number from the comments list.");
        }
    }

    /**
     * Recursively searches for a comment in the comment hierarchy
     * @param comments List of comments to search through
     * @param commentId UUID of the comment to find
     * @return The found comment or null if not found
     */
    private CommentResponseDto findCommentInHierarchy(List<CommentResponseDto> comments, UUID commentId) {
        for (CommentResponseDto comment : comments) {
            if (comment.id().equals(commentId)) {
                return comment;
            }
            // Recursively search in replies
            if (comment.replies() != null && !comment.replies().isEmpty()) {
                CommentResponseDto found = findCommentInHierarchy(comment.replies(), commentId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Handles comment editing with ownership validation
     * @param postId UUID of the post containing the comment
     */
    private void editComment(UUID postId) {
        String commentIdStr = ConsoleIO.readLine("Enter the comment ID you want to edit: ");
        try {
            int simpleCommentId = Integer.parseInt(commentIdStr);

            UUID commentId = commentIdMapping.get(simpleCommentId);
            if (commentId == null) {
                MenuFormatter.printErrorMessage("Invalid comment ID! Please choose a number from the comments list above.");
                return;
            }

            // Load and validate comment
            ApiResult<List<CommentResponseDto>> commentsResult = client.getCommentsForPost(postId);
            if (!commentsResult.success) {
                MenuFormatter.printErrorMessage("Error loading comments: " + commentsResult.message);
                return;
            }

            CommentResponseDto targetComment = findCommentInHierarchy(commentsResult.data, commentId);
            if (targetComment == null) {
                MenuFormatter.printErrorMessage("Comment not found!");
                return;
            }

            String currentUser = sessionManager.username();

            // Validate ownership
            if (!targetComment.author().equals(currentUser)) {
                MenuFormatter.printErrorMessage("You can only edit your own comments!");
                MenuFormatter.printInfoMessage("This comment belongs to: " + targetComment.author());
                return;
            }

            MenuFormatter.printInfoMessage("Current content: " + targetComment.content());
            String newContent = ConsoleIO.readLine("Enter new content: ");

            if (newContent.trim().isEmpty()) {
                MenuFormatter.printErrorMessage("Comment content cannot be empty!");
                return;
            }

            CommentUpdateRequestDto updateRequest = new CommentUpdateRequestDto(newContent);

            ApiResult<CommentResponseDto> result = client.updateComment(commentId, updateRequest);
            if (result.success) {
                MenuFormatter.printSuccessMessage("Comment updated successfully!");
                refreshCommentsDisplay(postId);
            } else {
                MenuFormatter.printErrorMessage("Error updating comment: " + result.message);
            }

        } catch (NumberFormatException e) {
            MenuFormatter.printErrorMessage("Please enter a valid number from the comments list.");
        }
    }

    /**
     * Handles comment deletion with ownership validation and confirmation
     * @param postId UUID of the post containing the comment
     */
    private void deleteComment(UUID postId) {
        String commentIdStr = ConsoleIO.readLine("Enter the comment ID you want to delete: ");
        try {
            int simpleCommentId = Integer.parseInt(commentIdStr);

            UUID commentId = commentIdMapping.get(simpleCommentId);
            if (commentId == null) {
                MenuFormatter.printErrorMessage("Invalid comment ID! Please choose a number from the comments list above.");
                return;
            }

            // Load and validate comment
            ApiResult<List<CommentResponseDto>> commentsResult = client.getCommentsForPost(postId);
            if (!commentsResult.success) {
                MenuFormatter.printErrorMessage("Error loading comments: " + commentsResult.message);
                return;
            }

            CommentResponseDto targetComment = findCommentInHierarchy(commentsResult.data, commentId);
            if (targetComment == null) {
                MenuFormatter.printErrorMessage("Comment not found!");
                return;
            }

            String currentUser = sessionManager.username();

            // Validate ownership
            if (!targetComment.author().equals(currentUser)) {
                MenuFormatter.printErrorMessage("You can only delete your own comments!");
                MenuFormatter.printInfoMessage("This comment belongs to: " + targetComment.author());
                return;
            }

            MenuFormatter.printInfoMessage("Comment content: " + targetComment.content());
            String confirm = ConsoleIO.readLine("Are you sure you want to delete this comment? (yes/no): ");

            if ("yes".equalsIgnoreCase(confirm)) {
                ApiResult<String> result = client.deleteComment(commentId);
                if (result.success) {
                    MenuFormatter.printSuccessMessage("Comment deleted successfully!");
                    refreshCommentsDisplay(postId);
                } else {
                    MenuFormatter.printErrorMessage("Error deleting comment: " + result.message);
                }
            } else {
                MenuFormatter.printInfoMessage("Delete cancelled.");
            }

        } catch (NumberFormatException e) {
            MenuFormatter.printErrorMessage("Please enter a valid number from the comments list.");
        }
    }
}
