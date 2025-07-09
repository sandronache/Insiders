package Post;

import Comment.CommentSection;

public class Post {
    private String content;
    private CommentSection comments;

    public Post(String content) {
        this.content = content;
        this.comments = new CommentSection();
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

    public void editContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
