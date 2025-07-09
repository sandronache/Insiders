package Comment;

import java.util.ArrayList;

public class Comment {
    private String content;
    private String username;
    private ArrayList<Comment> replies;
    private boolean isDeleted;

    public Comment(String content, String username) {
        this.content = content;
        this.username = username;
        this.replies = new ArrayList<>();
        this.isDeleted = false;
    }

    public void addReply(String id, String content, String username) {
        if (id.isEmpty()) {
            replies.add(new Comment(content, username));
            return;
        }
        int idx = Helper.extractFirstLevel(id);
        String remaining_id = Helper.extractRemainingLevels(id);

        replies.get(idx).addReply(remaining_id, content, username);
    }

    public void deleteReply(String id) {
        if (id.isEmpty()) {
            isDeleted = true;
            return;
        }
        int idx = Helper.extractFirstLevel(id);
        String remaining_id = Helper.extractRemainingLevels(id);

        replies.get(idx).deleteReply(remaining_id);
    }

    public void setDeleted() {
        this.isDeleted = true;
    }
}
