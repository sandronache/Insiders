package post;

import comment.Comment;
import comment.CommentSection;
import vote.Vote;
import Services.VotingService;
import Services.ContentService;

import java.util.ArrayList;

public class Post {
    private String content;
    private String username;
    private Vote votes;
    private CommentSection comments;
    private final VotingService votingService;
    private final ContentService contentService;

    public Post(String content, String username) {
        this.content = content;
        this.username = username;
        this.comments = new CommentSection();
        this.votes = new Vote();
        this.votingService = VotingService.getInstance();
        this.contentService = ContentService.getInstance();
    }

    /*
    public void getComments()
    {
        CommentService.getIsntance().getCommentsForId(this.id);
    }*/

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
        votingService.upvoteCommentByPath(this, id, username);
    }

    public void addDownVoteComment(String id, String username) {
        votingService.downvoteCommentByPath(this, id, username);
    }

    public void addUpVotePost(String username) {
        votingService.upvotePost(this, username);
    }

    public void addDownVotePost(String username) {
        votingService.downvotePost(this, username);
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
        return votingService.getPostUpvoteCount(this);
    }

    public int getDownVoteCount() {
        return votingService.getPostDownvoteCount(this);
    }

    public boolean isEmoji() {
        return votingService.isPostEmoji(this);
    }

    public ArrayList<Comment> getComments() {
        return comments.getComments();
    }
}
