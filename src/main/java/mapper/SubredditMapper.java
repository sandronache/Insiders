package main.java.mapper;

import main.java.dto.subreddit.SubredditCreateRequestDto;
import main.java.dto.subreddit.SubredditResponseDto;
import main.java.entity.Subreddit;
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
