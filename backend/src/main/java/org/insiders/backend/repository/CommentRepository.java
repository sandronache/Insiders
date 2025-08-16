package org.insiders.backend.repository;

import jakarta.persistence.QueryHint;
import org.insiders.backend.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPostId(UUID postId);

    List<Comment> findByParentCommentId(UUID parentCommentId);
    int countByPostId(UUID postId);

    @EntityGraph(attributePaths = {"user", "parentComment"})
    @QueryHints(@QueryHint(name = org.hibernate.annotations.QueryHints.READ_ONLY, value = "true"))
    List<Comment> findByPostIdOrderByCreatedAtDesc(UUID postId);
}
