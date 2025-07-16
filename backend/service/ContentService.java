package service;

import model.Post;
import model.Comment;
import utils.Helper;

import java.util.Map;

public class ContentService {
    private final VotingService votingService;
    private final CommentService commentService;

    public ContentService(VotingService votingService, CommentService commentService) {
        this.votingService = votingService;
        this.commentService = commentService;
    }

    public Post createPost(String content, String username) {
        return new Post(content, username, votingService.createVote());
    }

    public void addComment(Post post, String content, String username) {
        Comment newComment = commentService.createComment(content, username);
        post.addComment(newComment);
    }
    public void deleteCommentOrReply(Post post, String id) {
        Map<Integer, Comment> comments = post.getComments();

        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= comments.size()) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        commentService.deleteComment(comments.get(idx), remaining_id);
    }

    public void addReply(Post post, String id, String content, String username) {
        Map<Integer, Comment> comments = post.getComments();

        int idx = Helper.extractFirstLevel(id);
        if (!comments.containsKey(idx)) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        commentService.addReply(comments.get(idx), remaining_id, content, username);
    }

    public void addUpvoteComment(Post post, String id, String username) {
        Map<Integer, Comment> comments = post.getComments();

        int idx = Helper.extractFirstLevel(id);
        if (!comments.containsKey(idx)) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        commentService.addUpvote(comments.get(idx), remaining_id, username);
    }

    public void addDownvoteComment(Post post, String id, String username) {
        Map<Integer, Comment> comments = post.getComments();

        int idx = Helper.extractFirstLevel(id);
        if (!comments.containsKey(idx)) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        commentService.addDownvote(comments.get(idx), remaining_id, username);
    }

    public void addUpvotePost(Post post, String username) {
        votingService.addUpvote(post.getVote(), username);
    }
    public void addDownvotePost(Post post, String username) {
        votingService.addDownvote(post.getVote(), username);
    }

    public int getUpvoteCount(Post post) {
        return votingService.getUpvoteCount(post.getVote());
    }
    public int getDownvoteCount(Post post) {
        return votingService.getDownvoteCount(post.getVote());
    }

    public boolean isEmoji(Post post) {
        return votingService.isEmoji(post.getVote());
    }

    // rendering functions

    public String renderFeedPost(Post post, String id) {
        StringBuilder sb = new StringBuilder();

        sb.append("post ").append(id).append(" by (").append(post.getUsername()).append("):\n\n");
        if (isEmoji(post)) {
            sb.append("🔥 ");
        }
        sb.append(post.getContent()).append("\n\n");
        sb.append("upvotes = ").append(getUpvoteCount(post)).append("\n");
        sb.append("downvotes = ").append(getDownvoteCount(post)).append("\n\n");
        return sb.toString();
    }

    public String renderFullPost(Post post) {
        StringBuilder sb = new StringBuilder();

        sb.append('(').append(post.getUsername()).append("):\n");
        if (isEmoji(post)) {
            sb.append("🔥 ");
        }
        sb.append(post.getContent()).append("\n\n");
        sb.append("upvotes = ").append(getUpvoteCount(post)).append("\n");
        sb.append("downvotes = ").append(getDownvoteCount(post)).append("\n\n\n");
        for (Map.Entry<Integer, Comment> entry : post.getComments().entrySet()) {
            Integer id = entry.getKey();
            Comment comment = entry.getValue();
            commentService.renderComment(comment, sb, 1, id.toString());
        }
        return sb.toString();
    }

//    public void editPostContent(Post post, String newContent) {
//        if (post == null) return;
//        try {
//            java.lang.reflect.Method method = Post.class.getMethod("editContent", String.class);
//            method.invoke(post, newContent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void addComment(Post post, String content, String username) {
//        if (post == null) return;
//        try {
//            java.lang.reflect.Method method = Post.class.getMethod("addComment", String.class, String.class);
//            method.invoke(post, content, username);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void deleteComment(Post post, String commentId) {
//        if (post == null) return;
//        try {
//            java.lang.reflect.Method method = Post.class.getMethod("deleteComment", String.class);
//            method.invoke(post, commentId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void addReply(Post post, String commentId, String content, String username) {
//        if (post == null) return;
//        try {
//            java.lang.reflect.Method method = Post.class.getMethod("addReply", String.class, String.class, String.class);
//            method.invoke(post, commentId, content, username);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void deleteReply(Post post, String replyId) {
//        if (post == null) return;
//        try {
//            java.lang.reflect.Method method = Post.class.getMethod("deleteReply", String.class);
//            method.invoke(post, replyId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public ArrayList<Comment> getComments(Post post) {
//        if (post == null) return new ArrayList<>();
//        try {
//            java.lang.reflect.Method method = Post.class.getMethod("getComments");
//            return (ArrayList<Comment>) method.invoke(post);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
//
//    public Comment findComment(Post post, String commentPath) {
//        if (post == null || commentPath == null || commentPath.isEmpty()) {
//            return null;
//        }
//
//        try {
//            int idx = utils.Helper.extractFirstLevel(commentPath);
//            String remainingPath = utils.Helper.extractRemainingLevels(commentPath);
//
//            // Get top-level comments from the post
//            ArrayList<Comment> comments = getComments(post);
//
//            if (idx < 0 || idx >= comments.size()) {
//                return null;
//            }
//
//            Comment targetComment = comments.get(idx);
//
//            // If there are no more levels to navigate, return the current comment
//            if (remainingPath.isEmpty()) {
//                return targetComment;
//            }
//
//            // Otherwise, recursively navigate to the nested comment
//            return findNestedComment(targetComment, remainingPath);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    private Comment findNestedComment(Comment parentComment, String path) {
//        if (parentComment == null || path == null || path.isEmpty()) {
//            return parentComment;
//        }
//
//        try {
//            int idx = utils.Helper.extractFirstLevel(path);
//            String remainingPath = utils.Helper.extractRemainingLevels(path);
//
//            ArrayList<Comment> replies = parentComment.getReplies();
//
//            if (idx < 0 || idx >= replies.size()) {
//                return null;
//            }
//
//            Comment targetComment = replies.get(idx);
//
//            if (remainingPath.isEmpty()) {
//                return targetComment;
//            }
//
//            return findNestedComment(targetComment, remainingPath);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
}
