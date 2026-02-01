package com.communityhelp.app.user.service;

import com.communityhelp.app.user.dto.*;
import com.communityhelp.app.user.mapper.UserMapper;
import com.communityhelp.app.user.model.Role;
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
        user.setRole(Role.REQUESTER);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        //Si se envía la location
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            user.setLocation(dto.getLatitude(), dto.getLongitude());
        }

        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateUser(UUID id, UserUpdateRequestDto dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist with ID: " + id));

        if (dto.getName() != null) {
            existingUser.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            existingUser.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null) {
            existingUser.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            existingUser.setLocation(dto.getLatitude(), dto.getLongitude());
        }

        User savedUser = userRepository.save(existingUser);

        return userMapper.toDto(savedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }
}
