package org.insiders.backend.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "subreddits")
public class Subreddit {

    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 200)
    private String iconUrl;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "subreddit", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Post> posts = new ArrayList<>();

    public Subreddit() {}

    public Subreddit(String name, String displayName, String description, String iconUrl) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.iconUrl = iconUrl;
    }

    @PrePersist
    private void ensureId() {
        if (id == null) id = UUID.randomUUID();
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Subreddit that)) return false;
        return id != null && id.equals(that.id);
    }
    @Override public int hashCode() { return 31; }
}

