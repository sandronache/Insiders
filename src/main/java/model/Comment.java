package main.java.model;

import java.util.TreeMap;

public class Comment {
    private final String content;
    private final String username;
    private final Vote vote;
    private Integer idNextReply;
    private final TreeMap<Integer, Comment> replies;
    private boolean isDeleted;

    public Comment(String content, String username, Vote vote) {
        this.content = content;
        this.username = username;
        this.vote = vote;
        this.idNextReply = 0;
        this.replies = new TreeMap<>();
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

    public Integer getIdNextReply() {
        return idNextReply;
    }
    public void setIdNextReply(Integer id) {
        this.idNextReply = id;
    }

    public TreeMap<Integer, Comment> getReplies() {
        return replies;
    }

    public boolean isDeleted() {
        return isDeleted;
    }
    public void setIsDeleted(boolean value) {
        isDeleted = value;
    }
}
