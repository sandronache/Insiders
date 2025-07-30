package main.java.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.TreeMap;
import java.util.UUID;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @JsonProperty("author")
    private String username;

    @Column(nullable = false)
    private String subreddit;

    @Column(nullable = false)
    private Integer upvotes = 0;

    @Column(nullable = false)
    private Integer downvotes = 0;
    
    @JsonProperty("score")
    @Transient
    private Integer score; // Calculated as upvotes - downvotes

    @Column(nullable = false)
    private Integer commentCount = 0;

    @JsonProperty("userVote")
    @Transient
    private String currentUserVote; // "up", "down", or null

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Legacy fields for backwards compatibility
    @Transient
    private Vote vote;

    @Transient
    private Integer idNextComment = 0;

    @Transient
    private final TreeMap<Integer, Comment> comments = new TreeMap<>();

    // Default constructor for JPA
    public Post() {}

    // Constructor for API requests
    public Post(String title, String content, String username, String subreddit) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.content = content;
        this.username = username;
        this.subreddit = subreddit;
        this.upvotes = 1; // Auto-upvote by author
        this.downvotes = 0;
        this.commentCount = 0;
        this.currentUserVote = "up"; // Author automatically upvotes their post
        this.vote = new Vote();
    }

    // Legacy constructor for backwards compatibility
    public Post(String content, String username, Vote vote) {
        this.id = UUID.randomUUID();
        this.title = "Legacy Post"; // Default title for legacy posts
        this.content = content;
        this.username = username;
        this.subreddit = "general"; // Default subreddit
        this.vote = vote;
        this.upvotes = vote != null ? vote.getUpvote().size() : 0;
        this.downvotes = vote != null ? vote.getDownvote().size() : 0;
        this.commentCount = 0;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonProperty("author")
    public String getAuthor() {
        return username;
    }

    @JsonProperty("author")
    public void setAuthor(String author) {
        this.username = author;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public Integer getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    public Integer getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(Integer downvotes) {
        this.downvotes = downvotes;
    }

    @JsonProperty("score")
    public Integer getScore() {
        return upvotes - downvotes;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public String getCurrentUserVote() {
        return currentUserVote;
    }

    public void setCurrentUserVote(String currentUserVote) {
        this.currentUserVote = currentUserVote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Legacy getters for backwards compatibility
    public Vote getVote() {
        return vote;
    }

    public void setVote(Vote vote) {
        this.vote = vote;
        if (vote != null) {
            this.upvotes = vote.getUpvote().size();
            this.downvotes = vote.getDownvote().size();
        }
    }

    public Integer getIdNextComment() {
        return idNextComment;
    }

    public void setIdNextComment(Integer id) {
        this.idNextComment = id;
    }

    public TreeMap<Integer, Comment> getComments() {
        return comments;
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
