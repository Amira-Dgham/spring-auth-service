package com.example.mobelite.first_spring_boot_rest_api.controller;

import com.example.mobelite.first_spring_boot_rest_api.dto.payload.LoginResponse;
import com.example.mobelite.first_spring_boot_rest_api.dto.payload.LoginUserRequest;
import com.example.mobelite.first_spring_boot_rest_api.dto.payload.RegisterUserRequest;
import com.example.mobelite.first_spring_boot_rest_api.model.User;
import com.example.mobelite.first_spring_boot_rest_api.service.AuthenticationService;
import com.example.mobelite.first_spring_boot_rest_api.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserRequest registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserRequest loginUserRequest) {
        User authenticatedUser = authenticationService.authenticate(loginUserRequest);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = LoginResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return ResponseEntity.ok(loginResponse);
    }
}