package com.communityhelp.app.user.controller;

import com.communityhelp.app.security.AppUserDetails;
import com.communityhelp.app.user.dto.UserResponseDto;
import com.communityhelp.app.user.dto.UserUpdateRequestDto;
import com.communityhelp.app.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Users", description = "User profile management")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * @AuthenticationPrincipal - Spring Security inyecta el usuario ya autenticado
     * basado en el SecurityContext que se llena cuando el JWT es validado por el JwtAuthenticationFilter.
     */
    @Operation(summary = "Get my profile")
    @GetMapping(path = "/me")
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal AppUserDetails currentUser) {
        UserResponseDto userResponseDto = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(userResponseDto);
    }

    @Operation(summary = "Get all users", description = "Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    /**
     * @AuthenticationPrincipal - Spring Security inyecta el usuario ya autenticado
     */
    @Operation(summary = "Update my profile")
    @PatchMapping(path = "/me")
    public ResponseEntity<UserResponseDto> updateUser(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestBody UserUpdateRequestDto dto) {

        UserResponseDto updatedUser = userService.updateUser(currentUser.getId(), dto);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * @AuthenticationPrincipal - Spring Security inyecta el usuario ya autenticado
     */
    @Operation(summary = "Delete my account", description = "Soft delete — the user becomes inactive")
    @DeleteMapping(path = "/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal AppUserDetails currentUser) {
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * @PreAuthorize - autoriza el acceso al método antes de que se ejecute.
     */
    @Operation(summary = "Delete user by ID", description = "Admin only")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUserAsAdmin(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
