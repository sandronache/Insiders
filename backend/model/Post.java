package model;

import java.util.Map;
import java.util.LinkedHashMap;

public class Post {
    private String content;
    private String username;
    private Vote vote;
    private final Map<Integer, Comment> comments;

    public Post(String content, String username, Vote vote) {
        this.content = content;
        this.username = username;
        this.comments  = new LinkedHashMap<>();
        this.vote = vote;
    }

    public String getContent() {
        return content;
    }

    public String getUsername() {
        return username;
    }

    public Vote getVote() {
        return vote;
    }

    public Map<Integer, Comment> getComments() {
        return comments;
    }
    public void addComment(Comment comment) {
        int id = comments.size();
        comments.put(id, comment);
    }
}
