package model;

import java.util.Map;
import java.util.LinkedHashMap;

public class Comment {
    private final String content;
    private final String username;
    private final Vote vote;
    private final Map<Integer, Comment> replies;
    private boolean isDeleted;

    public Comment(String content, String username, Vote vote) {
        this.content = content;
        this.username = username;
        this.vote = vote;
        this.replies = new LinkedHashMap<>();
        this.isDeleted = false;
    }

    public String getContent() {
        if (isDeleted) return "[deleted]";
        else return content;
    }

    public String getUsername() {
        return username;
    }

    public Vote getVote() {
        return vote;
    }

    public Map<Integer, Comment> getReplies() {
        return replies;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
    public void setIsDeleted(boolean value) {
        isDeleted = value;
    }
}
