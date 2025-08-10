package org.insiders.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "votes")
public class Vote {
    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private Comment comment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "is_upvote", nullable = false)
    private boolean upvote;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Vote() {
    }

    public Vote(Post post, Comment comment, User user, boolean isUpvote) {
        this.id = UUID.randomUUID();
        this.post = post;
        this.comment = comment;
        this.user = user;
        this.upvote = isUpvote;
    }

    public UUID getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Comment getComment() {
        return comment;
    }

    public User getUser() {
        return user;
    }

    public boolean isUpvote() {
        return upvote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUpvote(boolean upvote) {
        this.upvote = upvote;
    }
}