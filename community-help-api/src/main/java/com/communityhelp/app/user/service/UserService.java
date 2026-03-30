package com.communityhelp.app.user.service;

import com.communityhelp.app.user.dto.*;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface UserService {

    UserResponseDto createUser(UserCreateRequestDto dto);
    UserResponseDto getUserByEmail(String email);
    UserResponseDto getUserById(UUID id);
    Page<UserResponseDto> getAllUsers(int page, int size);
    UserResponseDto updateUser(UUID id, UserUpdateRequestDto dto);
    void markEmailVerified(String email);
    void updatePassword(String email, String rawPassword);
    void deleteUser(UUID id);
    UserResponseDto getUserByEmailIncludeInactive(String email);
    UserResponseDto reactivateUser(UUID userId, UserCreateRequestDto dto);
}
