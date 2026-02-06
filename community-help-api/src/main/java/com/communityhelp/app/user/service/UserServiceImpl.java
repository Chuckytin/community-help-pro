package com.communityhelp.app.user.service;

import com.communityhelp.app.donation.repository.DonationRepository;
import com.communityhelp.app.helprequest.repository.HelpRequestRepository;
import com.communityhelp.app.user.dto.*;
import com.communityhelp.app.user.mapper.UserMapper;
import com.communityhelp.app.user.model.Role;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final DonationRepository donationRepository;
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
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        String reason = "Cancelled because user deleted account";

        // Libera voluntariado
        helpRequestRepository.releaseHelpRequestsAsVolunteer(id, reason);
        donationRepository.releaseDonationsAsVolunteer(id, reason);

        // Cancela sus propias donaciones y solicitudes
        helpRequestRepository.releaseHelpRequestsAsRequester(id, reason);
        donationRepository.releaseDonationsAsDonor(id, reason);

        // Soft delete
        user.setActive(false);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmailIncludeInactive(String email) {
        User user = userRepository.findByEmailIncludeInactive(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto reactivateUser(UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(true);
        user.setDeletedAt(null);
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        // Guarda el usuario existente, no crea uno nuevo
        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }
}
