package main.java.repository;

import main.java.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    Post getPostById(UUID postId);
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findBySubredditOrderByCreatedAtDesc(String subreddit);
}
