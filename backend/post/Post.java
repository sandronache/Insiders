package post;

import comment.Comment;
import comment.CommentSection;
import vote.Vote;

import java.util.ArrayList;

public class Post {
    private String content;
    private final String username;
    private final Vote votes;
    private final CommentSection comments;

    public Post(String content, String username) {
        this.content = content;
        this.username = username;
        this.comments = new CommentSection();
        this.votes = new Vote();
    }

    public void addComment(String content, String username) {
        comments.addComment(content, username);
    }

    public void deleteComment(String id) {
        comments.deleteComment(id);
    }

    public void addReply(String id, String content, String username) {
        comments.addReply(id, content, username);
    }

    public void deleteReply(String id) {
        comments.deleteReply(id);
    }

    public void addUpVoteComment(String id, String username) {
        comments.addUpVote(id, username);
    }

    public void addDownVoteComment(String id, String username) {
        comments.addDownVote(id, username);
    }

    public void addUpVotePost(String username) {
        votes.addUpVote(username);
    }

    public void addDownVotePost(String username) {
        votes.addDownVote(username);
    }

    public String getContent() {
        return content;
    }

    public void editContent(String content) {
        this.content = content;
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

    public ArrayList<Comment> getComments() {
        return comments.getComments();
    }
}
