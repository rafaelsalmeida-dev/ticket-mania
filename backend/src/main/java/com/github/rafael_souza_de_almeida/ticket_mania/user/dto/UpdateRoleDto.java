package com.github.rafael_souza_de_almeida.ticket_mania.user.dto;

import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleDto(@NotNull Role role) {
}
