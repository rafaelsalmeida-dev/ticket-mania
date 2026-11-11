package com.github.rafael_souza_de_almeida.ticket_mania.user.service;

import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.enums.Role;
import com.github.rafael_souza_de_almeida.ticket_mania.user.dto.RegisterDto;
import com.github.rafael_souza_de_almeida.ticket_mania.user.exception.UserAlreadyRegistered;
import com.github.rafael_souza_de_almeida.ticket_mania.user.exception.UserNotFoundException;
import com.github.rafael_souza_de_almeida.ticket_mania.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerUser(RegisterDto dto) {

        if(userRepository.findByEmail(dto.email()) != null) {
            throw new UserAlreadyRegistered("User already registered.");
        }

        User newUser = new User();

        newUser.setEmail(dto.email());
        newUser.setFullName(dto.name());
        newUser.setPassword(passwordEncoder.encode(dto.password()));
        newUser.setRole(Role.ROLE_USER);

        userRepository.save(newUser);

    }

    public void updateUserRole(UUID userId, Role newRole) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setRole(newRole);
        userRepository.save(user);

    }



}
