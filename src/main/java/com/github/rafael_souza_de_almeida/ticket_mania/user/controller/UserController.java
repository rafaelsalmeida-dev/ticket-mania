package com.github.rafael_souza_de_almeida.ticket_mania.user.controller;

import com.github.rafael_souza_de_almeida.ticket_mania.user.dto.UpdateRoleDto;
import com.github.rafael_souza_de_almeida.ticket_mania.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(@PathVariable UUID id, @RequestBody UpdateRoleDto dto) {

        userService.updateUserRole(id, dto.role());
        return ResponseEntity.noContent().build();

    }


}
