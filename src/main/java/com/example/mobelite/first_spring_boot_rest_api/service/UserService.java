package com.example.mobelite.first_spring_boot_rest_api.service;

import com.example.mobelite.first_spring_boot_rest_api.dto.payload.ProfileResponse;
import com.example.mobelite.first_spring_boot_rest_api.dto.payload.ValidateTokenResponse;
import com.example.mobelite.first_spring_boot_rest_api.model.User;
import com.example.mobelite.first_spring_boot_rest_api.repository.UserRepository;
import com.example.mobelite.first_spring_boot_rest_api.security.JwtService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Get the currently authenticated user's profile
     *
     * @return User object of the current authenticated user
     * @throws UsernameNotFoundException if the user is not found
     */
    public User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null) {
                logger.error("Authentication object is null");
                throw new UsernameNotFoundException("User not authenticated");
            }

            String email = authentication.getName();
            logger.debug("Attempting to retrieve user with email: {}", email);

            if (email == null || email.equals("anonymousUser")) {
                logger.error("Invalid email from authentication: {}", email);
                throw new UsernameNotFoundException("User not properly authenticated");
            }

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("User not found with email: {}", email);
                        return new UsernameNotFoundException("User not found with email: " + email);
                    });
        } catch (Exception e) {
            logger.error("Error retrieving current user: {}", e.getMessage(), e);
            throw new UsernameNotFoundException("Error retrieving user: " + e.getMessage());
        }
    }

    /**
     * Maps the current user entity to a ProfileResponse DTO
     *
     * @return ProfileResponse containing the user's profile information
     */
    public ProfileResponse getCurrentUserProfile() {
        try {
            User user = getCurrentUser();

            logger.debug("Building profile response for user: {}", user.getEmail());

            return ProfileResponse.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles())
                    .enabled(user.isEnabled())
                    .build();
        } catch (UsernameNotFoundException e) {
            logger.error("Failed to retrieve current user profile: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while retrieving user profile: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve user profile: " + e.getMessage(), e);
        }
    }

    /**
     * Validates a JWT token and returns validation response with user details
     *
     * @param token the JWT token to validate
     * @return ValidateTokenResponse containing validation result and user details
     */
    public ValidateTokenResponse validateToken(String token) {
        try {
            logger.debug("Validating token");

            // Remove "Bearer " prefix if present
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Validate token using TokenService
            boolean isValid = jwtService.isTokenValid(token);

            if (!isValid) {
                logger.warn("Token validation failed - token is invalid or expired");
                return ValidateTokenResponse.builder()
                        .isValid(false)
                        .message("Token is invalid or expired")
                        .build();
            }

            // Extract email from token
            String email = jwtService.extractUsername(token);
            logger.debug("Extracted email from token: {}", email);

            // Find user by email
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isEmpty()) {
                logger.warn("User not found for email: {}", email);
                return ValidateTokenResponse.builder()
                        .isValid(false)
                        .message("User not found")
                        .build();
            }

            User user = userOptional.get();

            // Check if user is enabled
            if (!user.isEnabled()) {
                logger.warn("User account is disabled for email: {}", email);
                return ValidateTokenResponse.builder()
                        .isValid(false)
                        .message("User account is disabled")
                        .build();
            }

            // Build successful response with user details
            ProfileResponse userProfile = ProfileResponse.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles())
                    .enabled(user.isEnabled())
                    .build();

            logger.debug("Token validation successful for user: {}", email);

            return ValidateTokenResponse.builder()
                    .isValid(true)
                    .message("Token is valid")
                    .user(userProfile)
                    .expiresAt(jwtService.getTokenExpirationDate(token))
                    .build();

        } catch (Exception e) {
            logger.error("Error during token validation: {}", e.getMessage(), e);
            return ValidateTokenResponse.builder()
                    .isValid(false)
                    .message("Token validation error: " + e.getMessage())
                    .build();
        }
    }
}


