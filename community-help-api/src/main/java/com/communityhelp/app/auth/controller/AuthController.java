package com.communityhelp.app.auth.controller;

import com.communityhelp.app.auth.dto.AuthResponse;
import com.communityhelp.app.auth.service.AuthService;
import com.communityhelp.app.user.dto.LoginRequestDto;
import com.communityhelp.app.user.dto.UserCreateRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Expone el endpoint de login que permite autenticarse al usuario y obtener un token JWT válido.
 */
@RestController
@RequestMapping(path = "/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(path = "/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserCreateRequestDto dto) {
        AuthResponse authResponse = authService.register(dto);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping(path = "/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequestDto dto) {
        AuthResponse authResponse = authService.login(dto);
        return ResponseEntity.ok(authResponse);
    }

}
