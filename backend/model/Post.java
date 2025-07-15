package model;

import java.util.ArrayList;

public class Post {
    private String content;
    private String username;
    private Vote vote;
    private ArrayList<Comment> comments;

    public Post(String content, String username, Vote vote) {
        this.content = content;
        this.username = username;
        this.comments = new ArrayList<>();
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

    public ArrayList<Comment> getComments() {
        return comments;
    }
}
