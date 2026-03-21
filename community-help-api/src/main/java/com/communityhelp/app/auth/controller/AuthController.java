package com.communityhelp.app.auth.controller;

import com.communityhelp.app.auth.dto.AuthResponse;
import com.communityhelp.app.auth.service.AuthService;
import com.communityhelp.app.user.dto.LoginRequestDto;
import com.communityhelp.app.user.dto.UserCreateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Expone los endpoints de registro y login.
 * Permite autenticarse al usuario y obtener un token JWT válido.
 */
@Tag(name = "Auth", description = "Registration and login. The JWT token obtained here is required for all other endpoints.")
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "200", description = "User registered and token generated")
    @PostMapping(path = "/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserCreateRequestDto dto) {
        AuthResponse authResponse = authService.register(dto);
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Login", description = "Returns a valid JWT token to use in other endpoints")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequestDto dto) {
        AuthResponse authResponse = authService.login(dto);
        return ResponseEntity.ok(authResponse);
    }

}
