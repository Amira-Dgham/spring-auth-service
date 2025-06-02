package com.example.mobelite.first_spring_boot_rest_api.service;

import com.example.mobelite.first_spring_boot_rest_api.dto.payload.LoginUserRequest;
import com.example.mobelite.first_spring_boot_rest_api.dto.payload.RegisterUserRequest;
import com.example.mobelite.first_spring_boot_rest_api.exception.ResourceNotFoundException;
import com.example.mobelite.first_spring_boot_rest_api.exception.UserAlreadyExistsException;
import com.example.mobelite.first_spring_boot_rest_api.model.ERole;
import com.example.mobelite.first_spring_boot_rest_api.model.Role;
import com.example.mobelite.first_spring_boot_rest_api.model.User;
import com.example.mobelite.first_spring_boot_rest_api.repository.RoleRepository;
import com.example.mobelite.first_spring_boot_rest_api.repository.UserRepository;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository userRepository,
                                 RoleRepository roleRepository,
                                 PasswordEncoder passwordEncoder,
                                 AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }


    public User signup(RegisterUserRequest request) {
        try {

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new UserAlreadyExistsException("Email already registered");
            }

            if (userRepository.existsByUsername(request.getUsername())) {
                throw new UserAlreadyExistsException("Username already registered");
            }

            Role userRole = roleRepository.findByName(
                            request.getRole() == null ? String.valueOf(ERole.ROLE_USER) : request.getRole())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(userRole);

            User user = User.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .enabled(false)
                    .roles(roles)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();

            return userRepository.save(user);
        } catch (UserAlreadyExistsException | ResourceNotFoundException ex) {
            // Propagate custom exceptions
            throw ex;
        } catch (Exception ex) {
            // Catch-all for unknown issues
            System.out.println("Unexpected error during signup: " + ex.getMessage());
            throw new RuntimeException("Signup failed", ex);
        }
    }
    public User authenticate(LoginUserRequest request) {
        // Method remains the same
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()
                    )
            );

            return userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.getEmail()));


        } catch (DisabledException | LockedException | BadCredentialsException ex) {
            // Propagate custom exceptions
            throw ex;
        } catch (Exception ex) {
            // Catch-all for unknown issues
            System.out.println("Unexpected error during signup: " + ex.getMessage());
            throw new RuntimeException("Signup failed", ex);
        }
    }
}