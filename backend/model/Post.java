package model;

import java.util.Map;
import java.util.TreeMap;

public class Post {
    private String content;
    private String username;
    private Vote vote;
    private Integer idNextComment;
    private final Map<Integer, Comment> comments;

    public Post(String content, String username, Vote vote) {
        this.content = content;
        this.username = username;
        this.idNextComment = 0;
        this.comments  = new TreeMap<>();
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

    public Integer getIdNextComment() {
        return idNextComment;
    }
    public void setIdNextComment(Integer id) {
        this.idNextComment = id;
    }

    public Map<Integer, Comment> getComments() {
        return comments;
    }
}
