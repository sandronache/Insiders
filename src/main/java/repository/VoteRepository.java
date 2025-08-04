package main.java.repository;

import main.java.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {
    Optional<Vote> findByUserIdAndCommentId(UUID userId, UUID commentId);
    void deleteByUserIdAndCommentId(UUID id, UUID commentId);
    int countByCommentIdAndUpvoteTrue(UUID commentId);
    int countByCommentIdAndUpvoteFalse(UUID commentId);
    void deleteByUserIdAndPostId(UUID userId, UUID postId);
    int countByPostIdAndUpvoteTrue(UUID postId);
    int countByPostIdAndUpvoteFalse(UUID postId);
    Optional<Vote> findByUserIdAndPostId(UUID userId, UUID postId);
}
