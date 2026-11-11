package com.github.rafael_souza_de_almeida.ticket_mania.user.service;

import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.enums.Role;
import com.github.rafael_souza_de_almeida.ticket_mania.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Test
    @DisplayName("loadUserByUsername - should return the user when the email exists")
    void shouldReturnUserWhenEmailExists() {
        User user = new User(UUID.randomUUID(), "Rafael Almeida", "rafael@test.com", "encodedPassword", Role.ROLE_USER);

        when(userRepository.findByEmail("rafael@test.com")).thenReturn(user);

        UserDetails result = authorizationService.loadUserByUsername("rafael@test.com");

        assertEquals(user, result);
    }

    @Test
    @DisplayName("loadUserByUsername - should throw UsernameNotFoundException when the email does not exist")
    void shouldThrowWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> authorizationService.loadUserByUsername("unknown@test.com"));
    }
}
