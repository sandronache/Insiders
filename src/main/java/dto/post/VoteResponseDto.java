package main.java.dto.post;

public record VoteResponseDto(
        int upvotes,
        int downvotes,
        int score,
        String voteType
) {
}
