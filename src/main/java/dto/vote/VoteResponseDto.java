package main.java.dto.vote;

public record VoteResponseDto(
        int upvotes,
        int downvotes,
        int score,
        String userVote
) {
}
