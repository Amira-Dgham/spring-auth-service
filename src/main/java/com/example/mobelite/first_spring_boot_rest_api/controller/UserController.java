package com.example.mobelite.first_spring_boot_rest_api.controller;

import com.example.mobelite.first_spring_boot_rest_api.dto.payload.ProfileResponse;
import com.example.mobelite.first_spring_boot_rest_api.dto.payload.ValidateTokenResponse;
import com.example.mobelite.first_spring_boot_rest_api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get the profile of the currently authenticated user
     * @return ResponseEntity containing the user profile information
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile() {
        ProfileResponse userProfile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(userProfile);
    }

    /**
     * Validate JWT token and return validation result with user details
     * @param request HTTP request to extract Authorization header
     * @return ResponseEntity containing token validation result
     */
    @PostMapping("/validate-token")
    public ResponseEntity<ValidateTokenResponse> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.trim().isEmpty()) {
            ValidateTokenResponse response = ValidateTokenResponse.builder()
                    .isValid(false)
                    .message("Authorization header is missing")
                    .build();
            return ResponseEntity.badRequest().body(response);
        }

        ValidateTokenResponse response = userService.validateToken(authHeader);

        // Return appropriate HTTP status based on validation result
        if (response.isValid()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

}