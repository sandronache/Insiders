package main.java.repository;

import main.java.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {


    // Find all posts ordered by creation date
    List<Post> findAllByOrderByCreatedAtDesc();


    // Custom query to find posts with vote counts (for better performance)
    @Query("SELECT p FROM Post p WHERE p.upvotes + p.downvotes >= :minVotes ORDER BY p.createdAt DESC")
    List<Post> findPostsWithMinimumVotes(@Param("minVotes") Integer minVotes);

    // Find popular posts (high score)
    @Query("SELECT p FROM Post p WHERE (p.upvotes - p.downvotes) >= :minScore ORDER BY (p.upvotes - p.downvotes) DESC")
    List<Post> findPopularPosts(@Param("minScore") Integer minScore);

    // Find recent posts in a subreddit with pagination
    @Query("SELECT p FROM Post p WHERE p.subreddit = :subreddit ORDER BY p.createdAt DESC")
    List<Post> findRecentPostsInSubreddit(@Param("subreddit") String subreddit, org.springframework.data.domain.Pageable pageable);

    @Query ("SELECT p FROM Post p WHERE p.username = :username ORDER BY p.createdAt DESC")
    List<Post> findByUsernameOrderByCreatedAtDesc(String username);

    // Helper class for backward compatibility with services that expect PostWithId
    public static class PostWithId {
        private final UUID databaseId;
        private final Post post;

        public PostWithId(UUID databaseId, Post post) {
            this.databaseId = databaseId;
            this.post = post;
        }

        public UUID getDatabaseId() {
            return databaseId;
        }

        public Post getPost() {
            return post;
        }
    }

    // Method for backward compatibility - convert JPA results to PostWithId format
    default List<PostWithId> findAllOrderedByDateWithIds() {
        List<Post> posts = findAllByOrderByCreatedAtDesc();
        return posts.stream()
                   .map(post -> new PostWithId(post.getId(), post))
                   .toList();
    }
}
