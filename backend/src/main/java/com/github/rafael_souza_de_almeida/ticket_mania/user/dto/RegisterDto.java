package com.github.rafael_souza_de_almeida.ticket_mania.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterDto(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
