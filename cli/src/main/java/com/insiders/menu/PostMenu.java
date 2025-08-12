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

public class PostMenu {
    private final PostClient client;
    private final SessionManager sessionManager;

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
            System.out.println("6. Add Comment");
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
                case 6 -> addComment(postId);
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
            System.out.println("ID: " + post.id());
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
        ApiResult<VoteResponseDto> result = client.upvotePost(postId);
        if (result.success) {
            VoteResponseDto vote = result.data;
            System.out.println("Vote successful! " + vote.message());
            System.out.println("Upvotes: " + vote.upvotes() + ", Downvotes: " + vote.downvotes());
        } else {
            System.out.println("Error voting: " + result.message);
        }
    }

    private void downvotePost(UUID postId) {
        ApiResult<VoteResponseDto> result = client.downvotePost(postId);
        if (result.success) {
            VoteResponseDto vote = result.data;
            System.out.println("Vote successful! " + vote.message());
            System.out.println("Upvotes: " + vote.upvotes() + ", Downvotes: " + vote.downvotes());
        } else {
            System.out.println("Error voting: " + result.message);
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
                System.out.println("\n--- All Comments (" + comments.size() + ") ---");
                for (CommentResponseDto comment : comments) {
                    System.out.println("Comment ID: " + comment.id());
                    System.out.println("Author: " + comment.author());

                    if (comment.author().equals(sessionManager.username())) {
                        System.out.println("💬 [YOUR COMMENT]");
                    }

                    System.out.println("Content: " + comment.content());
                    System.out.println("Created: " + comment.createdAt());
                    System.out.println("---");
                }
            }
        } else {
            System.out.println("Error loading comments: " + result.message + " (Status: " + result.status + ")");
        }
    }

    private void addComment(UUID postId) {
        String content = ConsoleIO.readLine("Enter your comment: ");
        String author = sessionManager.username();

        CommentCreateRequestDto commentRequest = new CommentCreateRequestDto(content, author);
        ApiResult<CommentResponseDto> result = client.createComment(postId, commentRequest);
        if (result.success) {
            System.out.println("Comment added successfully!");
            System.out.println("\n=== Updated Comments ===");
            viewComments(postId);
        } else {
            System.out.println("Error adding comment: " + result.message + " (Status: " + result.status + ")");
        }
    }
}
