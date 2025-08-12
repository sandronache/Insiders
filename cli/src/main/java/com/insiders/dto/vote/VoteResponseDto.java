package com.insiders.dto.vote;

public record VoteResponseDto(
        String message,
        int upvotes,
        int downvotes,
        String userVote
) {
}
