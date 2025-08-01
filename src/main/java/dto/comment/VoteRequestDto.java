package main.java.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record VoteRequestDto(
        @NotBlank(message = "votul trebuie specificat(up/down/none")
        String voteType
) {
}
