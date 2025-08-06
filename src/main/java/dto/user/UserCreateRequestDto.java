package main.java.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequestDto(
        @NotBlank(message =  "Username-ul este obligatoriu")
        @Size(min=3,max=30)
        String username,

        @NotBlank(message = "Email-ul este obligatoriu")
        @Email(message =  "Format invalid pentru email")
        String email,

        @NotBlank(message =  "Parola este obligatorie")
        String password
) {
}
