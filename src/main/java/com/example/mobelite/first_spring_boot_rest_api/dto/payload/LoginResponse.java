package com.example.mobelite.first_spring_boot_rest_api.dto.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class LoginResponse {
    private String token;
    private long expiresIn;
}