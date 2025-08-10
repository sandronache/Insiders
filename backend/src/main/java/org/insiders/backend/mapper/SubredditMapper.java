package org.insiders.backend.mapper;

import org.insiders.backend.dto.subreddit.SubredditResponseDto;
import org.insiders.backend.entity.Subreddit;
import org.springframework.stereotype.Component;

@Component
public class SubredditMapper {
    public SubredditResponseDto toDto(Subreddit subreddit,int postCount) {
        return new SubredditResponseDto(
            subreddit.getId(),
            subreddit.getName(),
            subreddit.getDisplayName(),
            subreddit.getDescription(),
            0,
            postCount,
            subreddit.getIconUrl(),
            subreddit.getCreatedAt()
        );
    }
}
