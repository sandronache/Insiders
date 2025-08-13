package com.insiders.menu;

import com.insiders.clients.PostClient;
import com.insiders.dto.comment.CommentCreateRequestDto;
import com.insiders.dto.comment.CommentResponseDto;
import com.insiders.dto.post.PostResponseDto;
import com.insiders.dto.post.PostUpdateRequestDto;
import com.insiders.dto.vote.VoteResponseDto;
import com.insiders.http.ApiResult;
import com.insiders.session.SessionManager;
import com.insiders.util.ConsoleIO;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class PostMenu {
    private final PostClient client;
    private final SessionManager sessionManager;
    private java.util.Map<Integer, UUID> commentIdMapping = new java.util.HashMap<>();

    public PostMenu(PostClient client, SessionManager sessionManager) {
        this.client = client;
        this.sessionManager = sessionManager;
    }

    public void showPostManagementMenu(UUID postId) {
        System.out.println("\n=== Post Details ===");
        viewPostDetails(postId);

        System.out.println("\n=== Comments ===");
        viewComments(postId);

        while (true) {
            System.out.println("\n--- Post Management ---");
            System.out.println("1. Refresh Post");
            System.out.println("2. Edit Post");
            System.out.println("3. Delete Post");
            System.out.println("4. Upvote Post");
            System.out.println("5. Downvote Post");
            System.out.println("6. Comment Actions");
            System.out.println("0. Back");

            int choice = ConsoleIO.readInt("Enter your choice:");
            switch (choice) {
                case 1 -> {
                    System.out.println("\n=== Post Details ===");
                    viewPostDetails(postId);
                    System.out.println("\n=== Comments ===");
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
                default -> System.out.println("Invalid choice. Please try again!");
            }
        }
    }

    private void viewPostDetails(UUID postId) {
        ApiResult<PostResponseDto> result = client.getPostById(postId);
        if (result.success) {
            PostResponseDto post = result.data;
            System.out.println("\n--- Post Details ---");
            System.out.println("Title: " + post.title());
            System.out.println("Content: " + post.content());
            System.out.println("Author: " + post.author());
            System.out.println("Subreddit: " + post.subreddit());
            System.out.println("Upvotes: " + post.upvotes());
            System.out.println("Downvotes: " + post.downvotes());
            System.out.println("Created: " + post.createdAt());
            System.out.println("Updated: " + post.updatedAt());
        } else {
            System.out.println("Error loading post: " + result.message);
        }
    }

    private void editPost(UUID postId) {
        String title = ConsoleIO.readLine("Enter new title (or press Enter to keep current): ");
        String content = ConsoleIO.readLine("Enter new content (or press Enter to keep current): ");

        PostUpdateRequestDto updateRequest = new PostUpdateRequestDto(
            title.isEmpty() ? null : title,
            content.isEmpty() ? null : content
        );

        ApiResult<PostResponseDto> result = client.updatePost(postId, updateRequest);
        if (result.success) {
            System.out.println("Post updated successfully!");
        } else {
            System.out.println("Error updating post: " + result.message);
        }
    }

    private boolean deletePost(UUID postId) {
        String confirm = ConsoleIO.readLine("Are you sure you want to delete this post? (yes/no): ");
        if ("yes".equalsIgnoreCase(confirm)) {
            ApiResult<String> result = client.deletePost(postId);
            if (result.success) {
                System.out.println("Post deleted successfully!");
                return true;
            } else {
                System.out.println("Error deleting post: " + result.message);
            }
        }
        return false;
    }

    private void upvotePost(UUID postId) {
        System.out.println("Upvoting post...");
        ApiResult<VoteResponseDto> result = client.upvotePost(postId);

        System.out.println("API Response - Success: " + result.success + ", Status: " + result.status + ", Message: " + result.message);

        if (result.success) {
            VoteResponseDto vote = result.data;
            if (vote != null) {
                System.out.println("✅ Post upvoted successfully!");
                System.out.println("Score: " + vote.upvotes() + "↑ " + vote.downvotes() + "↓");
            } else {
                System.out.println("✅ Vote successful but no vote data received.");
            }
        } else {
            System.out.println("Error voting: " + result.message + " (Status: " + result.status + ")");
        }
    }

    private void downvotePost(UUID postId) {
        System.out.println("Downvoting post...");
        ApiResult<VoteResponseDto> result = client.downvotePost(postId);

        System.out.println("API Response - Success: " + result.success + ", Status: " + result.status + ", Message: " + result.message);

        if (result.success) {
            VoteResponseDto vote = result.data;
            if (vote != null) {
                System.out.println("Post downvoted successfully!");
                System.out.println("Score: " + vote.upvotes() + "↑ " + vote.downvotes() + "↓");
            } else {
                System.out.println("Vote successful but no vote data received.");
            }
        } else {
            System.out.println("Error voting: " + result.message + " (Status: " + result.status + ")");
        }
    }

    private void viewComments(UUID postId) {
        System.out.println("Loading all comments for this post...");

        ApiResult<List<CommentResponseDto>> result = client.getCommentsForPost(postId);

        if (result.success) {
            List<CommentResponseDto> comments = result.data;
            if (comments == null) {
                System.out.println("No comments data received.");
            } else if (comments.isEmpty()) {
                System.out.println("No comments found for this post.");
            } else {
                commentIdMapping.clear();
                System.out.println("\n--- All Comments (" + comments.size() + ") ---");
                displayCommentsHierarchy(comments, 0, new AtomicInteger(1));
            }
        } else {
            System.out.println("Error loading comments: " + result.message + " (Status: " + result.status + ")");
        }
    }

    private void displayCommentsHierarchy(List<CommentResponseDto> comments, int indentLevel, AtomicInteger simpleIdCounter) {
        for (CommentResponseDto comment : comments) {
            int simpleId = simpleIdCounter.getAndIncrement();
            commentIdMapping.put(simpleId, comment.id());

            displaySingleComment(comment, indentLevel, simpleId);

            if (comment.replies() != null && !comment.replies().isEmpty()) {
                displayCommentsHierarchy(comment.replies(), indentLevel + 1, simpleIdCounter);
            }
        }
    }

    private void displaySingleComment(CommentResponseDto comment, int indentLevel, int simpleId) {
        String indent = "  ".repeat(indentLevel);
        String replyIndicator = indentLevel > 0 ? "↳ " : "";

        System.out.println(indent + replyIndicator + "Comment ID: " + simpleId);
        System.out.println(indent + "  Author: " + comment.author());

        if (comment.author().equals(sessionManager.username())) {
            System.out.println(indent + "  💬 [YOUR COMMENT]");
        }

        String voteStatus = "";
        if (comment.userVote() != null) {
            voteStatus = switch (comment.userVote()) {
                case "UPVOTE" -> " ⬆️ [YOU UPVOTED]";
                case "DOWNVOTE" -> " ⬇️ [YOU DOWNVOTED]";
                default -> "";
            };
        }

        System.out.println(indent + "  Content: " + comment.content());
        System.out.println(indent + "  Score: " + comment.score() + " (" + comment.upvotes() + "↑ " + comment.downvotes() + "↓)" + voteStatus);
        System.out.println(indent + "  Created: " + comment.createdAt());

        System.out.println(indent + "---");
    }

    private void commentActions(UUID postId) {
        System.out.println("\n=== Comment Actions ===");
        System.out.println("1. Add comment");
        System.out.println("2. Reply to existing comment");
        System.out.println("3. Upvote comment");
        System.out.println("4. Downvote comment");
        System.out.println("0. Back");

        int choice = ConsoleIO.readInt("Choose option: ");

        switch (choice) {
            case 1 -> createTopLevelComment(postId);
            case 2 -> createReplyComment(postId);
            case 3 -> voteOnComment(postId, "UPVOTE");
            case 4 -> voteOnComment(postId, "DOWNVOTE");
            case 0 -> System.out.println("Returning to post management...");
            default -> System.out.println("Invalid choice!");
        }
    }

    private void createTopLevelComment(UUID postId) {
        String content = ConsoleIO.readLine("Enter your comment: ");
        if (content.trim().isEmpty()) {
            System.out.println("Comment cannot be empty!");
            return;
        }

        String author = sessionManager.username();
        CommentCreateRequestDto commentRequest = new CommentCreateRequestDto(content, author, null);

        ApiResult<CommentResponseDto> result = client.createComment(postId, commentRequest);
        if (result.success) {
            System.out.println("Comment added successfully!");
            refreshCommentsDisplay(postId);
        } else {
            System.out.println("Error adding comment: " + result.message + " (Status: " + result.status + ")");
        }
    }

    private void createReplyComment(UUID postId) {
        String commentIdStr = ConsoleIO.readLine("Enter the comment ID you want to reply to: ");
        try {
            int simpleCommentId = Integer.parseInt(commentIdStr);

            UUID parentCommentId = commentIdMapping.get(simpleCommentId);
            if (parentCommentId == null) {
                System.out.println("Invalid comment ID! Please choose a number from the comments list above.");
                return;
            }

            String content = ConsoleIO.readLine("Enter your reply: ");
            if (content.trim().isEmpty()) {
                System.out.println("Reply cannot be empty!");
                return;
            }

            String author = sessionManager.username();
            CommentCreateRequestDto commentRequest = new CommentCreateRequestDto(content, author, parentCommentId);

            ApiResult<CommentResponseDto> result = client.createComment(postId, commentRequest);
            if (result.success) {
                System.out.println("Reply added successfully!");
                refreshCommentsDisplay(postId);
            } else {
                System.out.println("Error adding reply: " + result.message + " (Status: " + result.status + ")");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number from the comments list.");
        }
    }

    private void refreshCommentsDisplay(UUID postId) {
        System.out.println("\n=== Updated Comments ===");
        viewComments(postId);
    }

    private void voteOnComment(UUID postId, String voteType) {
        String commentIdStr = ConsoleIO.readLine("Enter the comment ID you want to " +
            (voteType.equals("UPVOTE") ? "upvote" : "downvote") + ": ");
        try {
            int simpleCommentId = Integer.parseInt(commentIdStr);

            UUID commentId = commentIdMapping.get(simpleCommentId);
            if (commentId == null) {
                System.out.println("Invalid comment ID! Please choose a number from the comments list above.");
                return;
            }

            ApiResult<VoteResponseDto> result = voteType.equals("UPVOTE") ?
                client.upvoteComment(commentId) : client.downvoteComment(commentId);

            if (result.success) {
                VoteResponseDto vote = result.data;
                String action = voteType.equals("UPVOTE") ? "upvoted" : "downvoted";
                System.out.println("Comment " + action + " successfully!");
                System.out.println("Score: " + vote.upvotes() + "↑ " + vote.downvotes() + "↓");
                refreshCommentsDisplay(postId);
            } else {
                System.out.println("Error voting on comment: " + result.message + " (Status: " + result.status + ")");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number from the comments list.");
        }
    }
}

// TODO Voting posts and comments (Andrei user hardcoded)
