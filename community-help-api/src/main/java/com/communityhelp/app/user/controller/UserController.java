package com.communityhelp.app.user.controller;

import com.communityhelp.app.security.AppUserDetails;
import com.communityhelp.app.user.dto.UserResponseDto;
import com.communityhelp.app.user.dto.UserUpdateRequestDto;
import com.communityhelp.app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * @AuthenticationPrincipal - Spring Security inyecta el usuario ya autenticado
     * basado en el SecurityContext que se llena cuando el JWT es validado por el JwtAuthenticationFilter.
     */
    @GetMapping(path = "/me")
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal AppUserDetails currentUser) {
        UserResponseDto userResponseDto = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(userResponseDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * @AuthenticationPrincipal - Spring Security inyecta el usuario ya autenticado
     */
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
    @DeleteMapping(path = "/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal AppUserDetails currentUser) {
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * @PreAuthorize - autoriza el acceso al método antes de que se ejecute.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUserAsAdmin(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
