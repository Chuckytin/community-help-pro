package com.communityhelp.app.user.service;

import com.communityhelp.app.user.dto.*;

import java.util.UUID;

public interface UserService {

    UserResponseDto createUser(UserCreateRequestDto dto);
    UserResponseDto getUserByEmail(String email);
    UserResponseDto getUserById(UUID id);
    UserResponseDto updateUser(UUID id, UserUpdateRequestDto dto);
    void deleteUser(UUID id);
}
