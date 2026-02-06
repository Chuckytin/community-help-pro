package com.communityhelp.app.user.service;

import com.communityhelp.app.user.dto.*;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponseDto createUser(UserCreateRequestDto dto);
    UserResponseDto getUserByEmail(String email);
    UserResponseDto getUserById(UUID id);
    List<UserResponseDto> getAllUsers();
    UserResponseDto updateUser(UUID id, UserUpdateRequestDto dto);
    void deleteUser(UUID id);
    UserResponseDto getUserByEmailIncludeInactive(String email);
    UserResponseDto reactivateUser(UUID userId, String newPassword);
}
