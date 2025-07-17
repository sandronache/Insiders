package main.java.model;

import java.util.HashSet;
import java.util.Set;

public class Vote {
    private final Set<String> upvote;
    private final Set<String> downvote;
    private boolean isEmoji;


    public Vote() {
        this.upvote = new HashSet<>();
        this.downvote = new HashSet<>();
        this.isEmoji = false;
    }

    public Set<String> getUpvote() {
        return upvote;
    }
    public Set<String> getDownvote() {
        return downvote;
    }

    public boolean isEmoji(){
        return isEmoji;
    }
    public void setEmoji(boolean value) {
        isEmoji = value;
    }

}