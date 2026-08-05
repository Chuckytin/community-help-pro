package com.communityhelp.app.auth.controller;

import com.communityhelp.app.auth.dto.AuthResponse;
import com.communityhelp.app.auth.dto.ForgotPasswordRequestDto;
import com.communityhelp.app.auth.dto.ResetPasswordRequestDto;
import com.communityhelp.app.auth.dto.VerifyEmailRequestDto;
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

    @Operation(summary = "Verify email with OTP code")
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequestDto dto) {
        authService.verifyEmail(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Request password reset OTP")
    @ApiResponse(responseCode = "200", description = "OTP sent if email exists")
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto dto) {
        authService.forgotPassword(dto.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reset password with OTP")
    @ApiResponse(responseCode = "200", description = "Password updated successfully")
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDto dto) {
        authService.resetPassword(dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Logout", description = "Invalidates the current JWT token so it can no longer be used")
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.ok().build();
    }

}
