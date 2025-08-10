package org.insiders.backend.repository;

import org.insiders.backend.entity.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubredditRepository extends JpaRepository<Subreddit, UUID> {
    boolean existsByNameIgnoreCase(String name);
    List<Subreddit> findAllByOrderByCreatedAtDesc();
    Optional<Subreddit> findByNameIgnoreCase(String name);
}
