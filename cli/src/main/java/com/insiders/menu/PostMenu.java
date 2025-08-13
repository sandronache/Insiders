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
            int score = post.upvotes() - post.downvotes();
            System.out.println("Score: " + score);
            System.out.println("Upvotes: " + post.upvotes());
            System.out.println("Downvotes: " + post.downvotes());
            System.out.println("Posted: " + TimeUtils.getRelativeTime(post.createdAt().toString()));
        } else {
            System.out.println("Error loading post: " + result.message);
        }
    }

    private void editPost(UUID postId) {
        ApiResult<PostResponseDto> postResult = client.getPostById(postId);
        if (!postResult.success) {
            System.out.println("Error loading post: " + postResult.message);
            return;
        }

        PostResponseDto post = postResult.data;
        String currentUser = sessionManager.username();

        if (!post.author().equals(currentUser)) {
            System.out.println("❌ You can only edit your own posts!");
            System.out.println("This post belongs to: " + post.author());
            return;
        }

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
        ApiResult<PostResponseDto> postResult = client.getPostById(postId);
        if (!postResult.success) {
            System.out.println("Error loading post: " + postResult.message);
            return false;
        }

        PostResponseDto post = postResult.data;
        String currentUser = sessionManager.username();

        if (!post.author().equals(currentUser)) {
            System.out.println("❌ You can only delete your own posts!");
            System.out.println("This post belongs to: " + post.author());
            return false;
        }

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
        System.out.println("Processing upvote...");

        ApiResult<PostResponseDto> postResult = client.getPostById(postId);
        if (!postResult.success) {
            System.out.println("Error checking post state: " + postResult.message);
            return;
        }

        String currentVote = postResult.data.userVote();
        System.out.println("DEBUG: Current user vote: '" + currentVote + "'");

        String voteType = "up";

        if ("up".equals(currentVote)) {
            voteType = "none";
            System.out.println("Removing your upvote...");
        } else {
            System.out.println("Upvoting post...");
        }

        System.out.println("DEBUG: Sending vote type: '" + voteType + "'");

        ApiResult<VoteResponseDto> result = client.votePost(postId, voteType);

        if (result.success) {
            VoteResponseDto vote = result.data;
            if (vote != null) {
                System.out.println("DEBUG: Response user vote: '" + vote.userVote() + "'");

                if ("none".equals(voteType)) {
                    System.out.println("✅ Upvote removed successfully!");
                } else {
                    System.out.println("✅ Post upvoted successfully!");
                }
                System.out.println("Score: " + vote.upvotes() + "↑ " + vote.downvotes() + "↓");

                if (vote.userVote() != null && !vote.userVote().isEmpty() && !"none".equals(vote.userVote())) {
                    String voteDisplay = vote.userVote().equals("up") ? "⬆️ YOU UPVOTED" :
                                       vote.userVote().equals("down") ? "⬇️ YOU DOWNVOTED" : "";
                    if (!voteDisplay.isEmpty()) {
                        System.out.println("Your vote: " + voteDisplay);
                    }
                } else {
                    System.out.println("Your vote: None");
                }
            } else {
                System.out.println("✅ Vote successful but no vote data received.");
            }
        } else {
            System.out.println("Error voting: " + result.message + " (Status: " + result.status + ")");
        }
    }

    private void downvotePost(UUID postId) {
        System.out.println("Processing downvote...");

        ApiResult<PostResponseDto> postResult = client.getPostById(postId);
        if (!postResult.success) {
            System.out.println("Error checking post state: " + postResult.message);
            return;
        }

        String currentVote = postResult.data.userVote();
        System.out.println("DEBUG: Current user vote: '" + currentVote + "'");

        String voteType = "down";

        if ("down".equals(currentVote)) {
            voteType = "none";
            System.out.println("Removing your downvote...");
        } else {
            System.out.println("Downvoting post...");
        }

        System.out.println("DEBUG: Sending vote type: '" + voteType + "'");

        ApiResult<VoteResponseDto> result = client.votePost(postId, voteType);

        if (result.success) {
            VoteResponseDto vote = result.data;
            if (vote != null) {
                System.out.println("DEBUG: Response user vote: '" + vote.userVote() + "'");

                if ("none".equals(voteType)) {
                    System.out.println("✅ Downvote removed successfully!");
                } else {
                    System.out.println("✅ Post downvoted successfully!");
                }
                System.out.println("Score: " + vote.upvotes() + "↑ " + vote.downvotes() + "↓");

                if (vote.userVote() != null && !vote.userVote().isEmpty() && !"none".equals(vote.userVote())) {
                    String voteDisplay = vote.userVote().equals("up") ? "⬆️ YOU UPVOTED" :
                                       vote.userVote().equals("down") ? "⬇️ YOU DOWNVOTED" : "";
                    if (!voteDisplay.isEmpty()) {
                        System.out.println("Your vote: " + voteDisplay);
                    }
                } else {
                    System.out.println("Your vote: None");
                }
            } else {
                System.out.println("✅ Vote successful but no vote data received.");
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
                case "up" -> " ⬆️ [YOU UPVOTED]";
                case "down" -> " ⬇️ [YOU DOWNVOTED]";
                default -> "";
            };
        }

        System.out.println(indent + "  Content: " + comment.content());
        System.out.println(indent + "  Score: " + comment.score() + " (" + comment.upvotes() + "↑ " + comment.downvotes() + "↓)" + voteStatus);
        System.out.println(indent + "  Posted: " + TimeUtils.getRelativeTime(comment.createdAt().toString()));

        System.out.println(indent + "---");
    }

    private void commentActions(UUID postId) {
        System.out.println("\n=== Comment Actions ===");
        System.out.println("1. Add comment");
        System.out.println("2. Reply to existing comment");
        System.out.println("3. Edit comment");
        System.out.println("4. Delete comment");
        System.out.println("5. Upvote comment");
        System.out.println("6. Downvote comment");
        System.out.println("0. Back");

        int choice = ConsoleIO.readInt("Choose option: ");

        switch (choice) {
            case 1 -> createTopLevelComment(postId);
            case 2 -> createReplyComment(postId);
            case 3 -> editComment(postId);
            case 4 -> deleteComment(postId);
            case 5 -> voteOnComment(postId, "UPVOTE");
            case 6 -> voteOnComment(postId, "DOWNVOTE");
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
            ApiResult<List<CommentResponseDto>> commentsResult = client.getCommentsForPost(postId);
            String currentVote = null;

            if (commentsResult.success && commentsResult.data != null) {
                CommentResponseDto targetComment = findCommentInHierarchy(commentsResult.data, commentId);
                if (targetComment != null) {
                    currentVote = targetComment.userVote();
                }
            }

            System.out.println("DEBUG: Current comment vote: '" + currentVote + "'");

            String actualVoteType = voteType.equals("UPVOTE") ? "up" : "down";

            if (actualVoteType.equals(currentVote)) {
                actualVoteType = "none";
                System.out.println("Removing your " + (voteType.equals("UPVOTE") ? "upvote" : "downvote") + "...");
            } else {
                System.out.println((voteType.equals("UPVOTE") ? "Upvoting" : "Downvoting") + " comment...");
            }

            System.out.println("DEBUG: Sending comment vote type: '" + actualVoteType + "'");

            ApiResult<VoteResponseDto> result = client.voteComment(commentId, actualVoteType);

            if (result.success) {
                VoteResponseDto vote = result.data;
                if (vote != null) {
                    System.out.println("DEBUG: Comment response user vote: '" + vote.userVote() + "'");

                    if ("none".equals(actualVoteType)) {
                        System.out.println("✅ " + (voteType.equals("UPVOTE") ? "Upvote" : "Downvote") + " removed successfully!");
                    } else {
                        System.out.println("✅ Comment " + (voteType.equals("UPVOTE") ? "upvoted" : "downvoted") + " successfully!");
                    }
                    System.out.println("Score: " + vote.upvotes() + "↑ " + vote.downvotes() + "↓");

                    if (vote.userVote() != null && !vote.userVote().isEmpty() && !"none".equals(vote.userVote())) {
                        String voteDisplay = vote.userVote().equals("up") ? "⬆️ YOU UPVOTED" :
                                           vote.userVote().equals("down") ? "⬇️ YOU DOWNVOTED" : "";
                        if (!voteDisplay.isEmpty()) {
                            System.out.println("Your vote: " + voteDisplay);
                        }
                    } else {
                        System.out.println("Your vote: None");
                    }
                } else {
                    System.out.println("✅ Vote successful but no vote data received.");
                }
                refreshCommentsDisplay(postId);
            } else {
                System.out.println("Error voting on comment: " + result.message + " (Status: " + result.status + ")");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number from the comments list.");
        }
    }

    private CommentResponseDto findCommentInHierarchy(List<CommentResponseDto> comments, UUID commentId) {
        for (CommentResponseDto comment : comments) {
            if (comment.id().equals(commentId)) {
                return comment;
            }
            if (comment.replies() != null && !comment.replies().isEmpty()) {
                CommentResponseDto found = findCommentInHierarchy(comment.replies(), commentId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void editComment(UUID postId) {
        String commentIdStr = ConsoleIO.readLine("Enter the comment ID you want to edit: ");
        try {
            int simpleCommentId = Integer.parseInt(commentIdStr);

            UUID commentId = commentIdMapping.get(simpleCommentId);
            if (commentId == null) {
                System.out.println("Invalid comment ID! Please choose a number from the comments list above.");
                return;
            }

            ApiResult<List<CommentResponseDto>> commentsResult = client.getCommentsForPost(postId);
            if (!commentsResult.success) {
                System.out.println("Error loading comments: " + commentsResult.message);
                return;
            }

            CommentResponseDto targetComment = findCommentInHierarchy(commentsResult.data, commentId);
            if (targetComment == null) {
                System.out.println("Comment not found!");
                return;
            }

            String currentUser = sessionManager.username();

            if (!targetComment.author().equals(currentUser)) {
                System.out.println("❌ You can only edit your own comments!");
                System.out.println("This comment belongs to: " + targetComment.author());
                return;
            }

            System.out.println("Current content: " + targetComment.content());
            String newContent = ConsoleIO.readLine("Enter new content: ");

            if (newContent.trim().isEmpty()) {
                System.out.println("Comment content cannot be empty!");
                return;
            }

            CommentUpdateRequestDto updateRequest = new CommentUpdateRequestDto(newContent);

            ApiResult<CommentResponseDto> result = client.updateComment(commentId, updateRequest);
            if (result.success) {
                System.out.println("✅ Comment updated successfully!");
                refreshCommentsDisplay(postId);
            } else {
                System.out.println("Error updating comment: " + result.message);
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number from the comments list.");
        }
    }

    private void deleteComment(UUID postId) {
        String commentIdStr = ConsoleIO.readLine("Enter the comment ID you want to delete: ");
        try {
            int simpleCommentId = Integer.parseInt(commentIdStr);

            UUID commentId = commentIdMapping.get(simpleCommentId);
            if (commentId == null) {
                System.out.println("Invalid comment ID! Please choose a number from the comments list above.");
                return;
            }

            ApiResult<List<CommentResponseDto>> commentsResult = client.getCommentsForPost(postId);
            if (!commentsResult.success) {
                System.out.println("Error loading comments: " + commentsResult.message);
                return;
            }

            CommentResponseDto targetComment = findCommentInHierarchy(commentsResult.data, commentId);
            if (targetComment == null) {
                System.out.println("Comment not found!");
                return;
            }

            String currentUser = sessionManager.username();

            if (!targetComment.author().equals(currentUser)) {
                System.out.println("❌ You can only delete your own comments!");
                System.out.println("This comment belongs to: " + targetComment.author());
                return;
            }

            System.out.println("Comment content: " + targetComment.content());
            String confirm = ConsoleIO.readLine("Are you sure you want to delete this comment? (yes/no): ");

            if ("yes".equalsIgnoreCase(confirm)) {
                ApiResult<String> result = client.deleteComment(commentId);
                if (result.success) {
                    System.out.println("✅ Comment deleted successfully!");
                    refreshCommentsDisplay(postId);
                } else {
                    System.out.println("Error deleting comment: " + result.message);
                }
            } else {
                System.out.println("Delete cancelled.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number from the comments list.");
        }
    }
}
