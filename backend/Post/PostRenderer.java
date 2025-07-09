package Post;

import Comment.Comment;

public class PostRenderer {
    public static String render(Post post) {
        StringBuilder sb = new StringBuilder();
        sb.append('(').append(post.getUsername()).append("):\n");
        sb.append(post.getContent()).append("\n\n");
        sb.append("upvotes = ").append(post.getUpVoteCount()).append("\n");
        sb.append("downvotes = ").append(post.getDownVoteCount()).append("\n\n\n");
        for (int i = 0; i < post.getComments().size(); i++) {
            renderComment(post.getComments().get(i), sb, 1, String.valueOf(i));
        }
        return sb.toString();
    }

    public static void renderComment(Comment comment, StringBuilder sb, int depth, String id) {
        sb.append("  ".repeat(depth)).append("-> [").append(id).append("] ");
        sb.append('(').append(comment.getUsername()).append(')');
        sb.append("  ").append(comment.getContent()).append('\n');

        sb.append("   ".repeat(depth));
        sb.append("upvotes = ").append(comment.getUpVoteCount()).append("\n");

        sb.append("   ".repeat(depth));
        sb.append("downvotes = ").append(comment.getDownVoteCount()).append("\n\n\n");

        for (int i = 0; i < comment.getReplies().size(); i++) {
            renderComment(comment.getReplies().get(i), sb, depth + 1, id + '.' + String.valueOf(i));
        }
    }
}
