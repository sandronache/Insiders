package Comment;

import Vote.Vote;

import java.util.ArrayList;

public class Comment {
    private String content;
    private String username;
    private Vote votes;
    private ArrayList<Comment> replies;
    private boolean isDeleted;

    public Comment(String content, String username) {
        this.content = content;
        this.username = username;
        this.votes = new Vote();
        this.replies = new ArrayList<>();
        this.isDeleted = false;
    }

    public void addReply(String id, String content, String username) {
        if (id.isEmpty()) {
            replies.add(new Comment(content, username));
            return;
        }
        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= replies.size()) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        replies.get(idx).addReply(remaining_id, content, username);
    }

    public void deleteReply(String id) {
        if (id.isEmpty()) {
            isDeleted = true;
            return;
        }
        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= replies.size()) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        replies.get(idx).deleteReply(remaining_id);
    }

    public void addUpVote(String id, String username) {
        if (id.isEmpty()) {
            votes.addUpVote(username);
            return;
        }
        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= replies.size()) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        replies.get(idx).addUpVote(remaining_id, username);
    }

    public void addDownVote(String id, String username) {
        if (id.isEmpty()) {
            votes.addDownVote(username);
            return;
        }
        int idx = Helper.extractFirstLevel(id);
        if (idx < 0 || idx >= replies.size()) return;

        String remaining_id = Helper.extractRemainingLevels(id);

        replies.get(idx).addDownVote(remaining_id, username);
    }

    public void setDeleted() {
        this.isDeleted = true;
    }

    public String getContent() {
        if (isDeleted) return "[deleted]";
        else return content;
    }

    public String getUsername() {
        return username;
    }

    public int getUpVoteCount() {
        return votes.getUpvoteCount();
    }

    public int getDownVoteCount() {
        return votes.getDownvoteCount();
    }

    public boolean isEmoji() {
        return votes.isEmoji();
    }

    public ArrayList<Comment> getReplies() {
        return replies;
    }
}
