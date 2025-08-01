package main.java.dto.comment;

public record VoteResponseDto(
        int upvotes,
        int downvotes,
        int score,
        String userVote
) {
}
