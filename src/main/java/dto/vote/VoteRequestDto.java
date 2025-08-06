package main.java.dto.vote;

import jakarta.validation.constraints.NotBlank;

public record VoteRequestDto(
        @NotBlank(message = "Tipul votului este obligatoriu(up/down/none)")
        String voteType
) {
}
