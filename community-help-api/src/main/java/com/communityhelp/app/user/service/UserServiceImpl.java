package com.communityhelp.app.user.service;

import com.communityhelp.app.user.dto.*;
import com.communityhelp.app.user.mapper.UserMapper;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto createUser(UserCreateRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already in use!");
        }

        User user = userMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    @Override
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateUser(UUID id, UserUpdateRequestDto dto) {
        User existingPost = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist with ID: " + id));

        existingPost.setName(dto.getName());
        existingPost.setEmail(dto.getEmail());
        existingPost.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        User savedUser = userRepository.save(existingPost);

        return userMapper.toDto(savedUser);
    }
}
