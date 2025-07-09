package Post;

import Comment.Comment;
import Comment.CommentSection;
import Vote.Vote;

import java.util.ArrayList;

public class Post {
    private String content;
    private String username;
    private Vote votes;
    private CommentSection comments;

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
    @Override
    public String toString() {
        return "Post{" +
                "content='" + content + '\'' +
                ", username='" + username + '\'' +
                ", votes=" + votes.getUpvoteCount() + " upvotes, " + votes.getDownvoteCount() + " downvotes" +
                ", comments=" + comments.getComments().size() +
                '}';
    }
    //Implemented toString method for better readability in App.java
}
