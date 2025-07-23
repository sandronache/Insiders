package main.java.model;

import java.util.TreeMap;

public class Comment {
    private Integer databaseId; // ID-ul din baza de date
    private final String content;
    private final String username;
    private final Vote vote;
    private Integer idNextReply;
    private final TreeMap<Integer, Comment> replies;
    private boolean isDeleted;

    public Comment(String content, String username, Vote vote) {
        this.databaseId = null; // Va fi setat când se salvează/încarcă din DB
        this.content = content;
        this.username = username;
        this.vote = vote;
        this.idNextReply = 0;
        this.replies = new TreeMap<>();
        this.isDeleted = false;
    }

    // Getter și setter pentru database ID
    public Integer getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Integer databaseId) {
        this.databaseId = databaseId;
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
