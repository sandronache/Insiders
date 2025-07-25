package main.java.repository;

import main.java.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {


    // Find all posts ordered by creation date
    List<Post> findAllByOrderByCreatedAtDesc();


    @Query ("SELECT p FROM Post p WHERE p.username = :username ORDER BY p.createdAt DESC")
    List<Post> findByUsernameOrderByCreatedAtDesc(String username);

    // Helper class for backward compatibility with services that expect PostWithId
    class PostWithId {
        private final UUID databaseId;
        private final Post post;

        public PostWithId(UUID databaseId, Post post) {
            this.databaseId = databaseId;
            this.post = post;
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
