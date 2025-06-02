package com.example.mobelite.first_spring_boot_rest_api.dto.payload;

import com.example.mobelite.first_spring_boot_rest_api.model.ERole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterUserRequest {

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private String password;

    private String role;

}