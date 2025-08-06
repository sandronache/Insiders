package main.java.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private int hashedPassword;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public User() {
    }

    public User(String username, String email, int hashedPassword) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getHashedPassword() {
        return hashedPassword;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}