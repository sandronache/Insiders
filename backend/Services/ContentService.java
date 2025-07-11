package Services;

import post.Post;
import comment.Comment;
import comment.CommentSection;

import java.util.ArrayList;

public class ContentService {
    private static ContentService instance;

    // Private constructor for singleton pattern
    private ContentService() {}

    public static synchronized ContentService getInstance() {
        if (instance == null) {
            instance = new ContentService();
        }
        return instance;
    }
    public Post createPost(String content, String username) {
        return new Post(content, username);
    }

    public void editPostContent(Post post, String newContent) {
        if (post == null) return;
        try {
            java.lang.reflect.Method method = Post.class.getMethod("editContent", String.class);
            method.invoke(post, newContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addComment(Post post, String content, String username) {
        if (post == null) return;
        try {
            java.lang.reflect.Method method = Post.class.getMethod("addComment", String.class, String.class);
            method.invoke(post, content, username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteComment(Post post, String commentId) {
        if (post == null) return;
        try {
            java.lang.reflect.Method method = Post.class.getMethod("deleteComment", String.class);
            method.invoke(post, commentId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addReply(Post post, String commentId, String content, String username) {
        if (post == null) return;
        try {
            java.lang.reflect.Method method = Post.class.getMethod("addReply", String.class, String.class, String.class);
            method.invoke(post, commentId, content, username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteReply(Post post, String replyId) {
        if (post == null) return;
        try {
            java.lang.reflect.Method method = Post.class.getMethod("deleteReply", String.class);
            method.invoke(post, replyId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Comment> getComments(Post post) {
        if (post == null) return new ArrayList<>();
        try {
            java.lang.reflect.Method method = Post.class.getMethod("getComments");
            return (ArrayList<Comment>) method.invoke(post);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Comment findComment(Post post, String commentPath) {
        if (post == null || commentPath == null || commentPath.isEmpty()) {
            return null;
        }

        try {
            int idx = utils.Helper.extractFirstLevel(commentPath);
            String remainingPath = utils.Helper.extractRemainingLevels(commentPath);

            // Get top-level comments from the post
            ArrayList<Comment> comments = getComments(post);

            if (idx < 0 || idx >= comments.size()) {
                return null;
            }

            Comment targetComment = comments.get(idx);

            // If there are no more levels to navigate, return the current comment
            if (remainingPath.isEmpty()) {
                return targetComment;
            }

            // Otherwise, recursively navigate to the nested comment
            return findNestedComment(targetComment, remainingPath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Comment findNestedComment(Comment parentComment, String path) {
        if (parentComment == null || path == null || path.isEmpty()) {
            return parentComment;
        }

        try {
            int idx = utils.Helper.extractFirstLevel(path);
            String remainingPath = utils.Helper.extractRemainingLevels(path);

            ArrayList<Comment> replies = parentComment.getReplies();

            if (idx < 0 || idx >= replies.size()) {
                return null;
            }

            Comment targetComment = replies.get(idx);

            if (remainingPath.isEmpty()) {
                return targetComment;
            }

            return findNestedComment(targetComment, remainingPath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
