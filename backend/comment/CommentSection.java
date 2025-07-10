package comment;

import utils.Helper;

import java.util.ArrayList;

public class CommentSection {
    private final ArrayList<Comment> comments;

    public CommentSection() {
        this.comments = new ArrayList<>();
    }

    public void addComment(String content, String username) {
        comments.add(new Comment(content, username));
    }

    public void deleteComment(String id) {
        int idx = Integer.parseInt(id);
        if (idx < 0 || idx >= comments.size()) return;
        comments.get(idx).setDeleted();
    }

    public void addReply(String id, String content, String username) {
        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= comments.size()) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        comments.get(idx).addReply(remaining_id, content, username);
    }

    public void deleteReply(String id) {
        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= comments.size()) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        comments.get(idx).deleteReply(remaining_id);
    }

    public void addUpVote(String id, String username) {
        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= comments.size()) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        comments.get(idx).addUpVote(remaining_id, username);
    }

    public void addDownVote(String id, String username) {
        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= comments.size()) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        comments.get(idx).addDownVote(remaining_id, username);
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }
}
