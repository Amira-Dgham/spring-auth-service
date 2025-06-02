package com.example.mobelite.first_spring_boot_rest_api.dto.payload;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO for token validation response
 * Contains validation result and user details if token is valid
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateTokenResponse {

    /**
     * Whether the token is valid or not
     */
    private boolean isValid;

    /**
     * Message describing the validation result
     * Examples: "Token is valid", "Token is expired", "User not found"
     */
    private String message;

    /**
     * User details if token is valid
     * Only included when isValid = true
     */
    private ProfileResponse user;

    /**
     * Token expiration date if token is valid
     * Format: yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date expiresAt;

    /**
     * Timestamp when validation was performed
     * Always included in the response
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @Builder.Default
    private Date validatedAt = new Date();

    /**
     * Token type (e.g., "Bearer")
     * Optional field for additional token information
     */
    private String tokenType;

    /**
     * Remaining time until token expires (in milliseconds)
     * Only included when token is valid
     */
    private Long remainingTimeMs;

    /**
     * Token issued at timestamp
     * Only included when token is valid
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date issuedAt;

    /**
     * Additional error code for specific validation failures
     * Examples: "EXPIRED", "MALFORMED", "USER_NOT_FOUND", "USER_DISABLED"
     */
    private String errorCode;

    // Convenience methods for common validation results

    /**
     * Create a successful validation response
     */
    public static ValidateTokenResponse success(ProfileResponse user, Date expiresAt) {
        return ValidateTokenResponse.builder()
                .isValid(true)
                .message("Token is valid")
                .user(user)
                .expiresAt(expiresAt)
                .tokenType("Bearer")
                .build();
    }

    /**
     * Create a failed validation response
     */
    public static ValidateTokenResponse failure(String message, String errorCode) {
        return ValidateTokenResponse.builder()
                .isValid(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }

    /**
     * Create a failed validation response with simple message
     */
    public static ValidateTokenResponse failure(String message) {
        return ValidateTokenResponse.builder()
                .isValid(false)
                .message(message)
                .build();
    }
}