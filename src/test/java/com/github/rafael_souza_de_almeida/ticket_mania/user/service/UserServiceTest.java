package com.github.rafael_souza_de_almeida.ticket_mania.user.service;

import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.enums.Role;
import com.github.rafael_souza_de_almeida.ticket_mania.user.dto.RegisterDto;
import com.github.rafael_souza_de_almeida.ticket_mania.user.exception.UserAlreadyRegistered;
import com.github.rafael_souza_de_almeida.ticket_mania.user.exception.UserNotFoundException;
import com.github.rafael_souza_de_almeida.ticket_mania.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("registerUser - should encode the password and persist a new user with ROLE_USER")
    void shouldRegisterNewUser() {
        RegisterDto dto = new RegisterDto("Rafael Almeida", "rafael@test.com", "plainPassword");

        when(userRepository.findByEmail(dto.email())).thenReturn(null);
        when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword");

        userService.registerUser(dto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(dto.email(), savedUser.getEmail());
        assertEquals(dto.name(), savedUser.getFullName());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(Role.ROLE_USER, savedUser.getRole());
    }

    @Test
    @DisplayName("registerUser - should throw UserAlreadyRegistered when the email is already in use")
    void shouldThrowWhenEmailAlreadyRegistered() {
        RegisterDto dto = new RegisterDto("Rafael Almeida", "rafael@test.com", "plainPassword");

        User existingUser = new User(UUID.randomUUID(), "Rafael Almeida", dto.email(), "hashed", Role.ROLE_USER);
        when(userRepository.findByEmail(dto.email())).thenReturn(existingUser);

        assertThrows(UserAlreadyRegistered.class, () -> userService.registerUser(dto));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserRole - should update the role of an existing user")
    void shouldUpdateUserRole() {
        UUID userId = UUID.randomUUID();
        User existingUser = new User(userId, "Rafael Almeida", "rafael@test.com", "hashed", Role.ROLE_USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.updateUserRole(userId, Role.ROLE_ADMIN);

        assertEquals(Role.ROLE_ADMIN, existingUser.getRole());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    @DisplayName("updateUserRole - should throw UserNotFoundException when user does not exist")
    void shouldThrowWhenUserNotFoundOnRoleUpdate() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUserRole(userId, Role.ROLE_ADMIN));

        verify(userRepository, never()).save(any(User.class));
    }
}
