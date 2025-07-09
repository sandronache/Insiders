package Comment;

import java.util.ArrayList;

public class CommentSection {
    private ArrayList<Comment> comments;

    public CommentSection() {
        this.comments = new ArrayList<>();
    }

    public void addComment(String content, String username) {
        comments.add(new Comment(content, username));
    }

    public void deleteComment(String id) {
        int idx = Integer.parseInt(id);
        comments.get(idx).setDeleted();
    }

    public void addReply(String id, String content, String username) {
        int idx = Helper.extractFirstLevel(id);
        String remaining_id = Helper.extractRemainingLevels(id);

        comments.get(idx).addReply(remaining_id, content, username);
    }

    public void deleteReply(String id) {
        int idx = Helper.extractFirstLevel(id);
        String remaining_id = Helper.extractRemainingLevels(id);

        comments.get(idx).deleteReply(remaining_id);
    }
}
