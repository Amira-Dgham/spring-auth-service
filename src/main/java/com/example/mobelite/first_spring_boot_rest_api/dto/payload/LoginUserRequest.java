package com.example.mobelite.first_spring_boot_rest_api.dto.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data

public class LoginUserRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}