package com.communityhelp.app.user.service;

import com.communityhelp.app.donation.model.Donation;
import com.communityhelp.app.donation.repository.DonationRepository;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.helprequest.repository.HelpRequestRepository;
import com.communityhelp.app.proposal.model.ProposalCancelReason;
import com.communityhelp.app.proposal.repository.ProposalRepository;
import com.communityhelp.app.user.dto.UserCreateRequestDto;
import com.communityhelp.app.user.dto.UserResponseDto;
import com.communityhelp.app.user.dto.UserUpdateRequestDto;
import com.communityhelp.app.user.exception.DuplicateEmailException;
import com.communityhelp.app.user.mapper.UserMapper;
import com.communityhelp.app.user.model.Role;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final HelpRequestRepository helpRequestRepository;
    private final DonationRepository donationRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ProposalRepository proposalRepository;

    @Override
    public UserResponseDto createUser(UserCreateRequestDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEmailException();
        }

        User user = userMapper.toEntity(dto);
        user.setRole(Role.USER);
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
    public Page<UserResponseDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
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
    public void markEmailVerified(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public void updatePassword(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    @Override
    public void deleteUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        String reason = "Cancelled because user deleted account";

        // Cancela solicitudes creadas por el usuario y sus proposals asociadas
        List<HelpRequest> helpRequests = helpRequestRepository.findByRequester_Id(id);

        helpRequests.forEach(hr -> {
            hr.cancel(reason);
            proposalRepository.cancelPendingProposals(hr.getId(), ProposalCancelReason.TARGET_ENTITY_CANCELLED);
        });

        // Cancela donaciones creadas por el usuario y sus proposals asociadas
        List<Donation> donations = donationRepository.findByDonor_Id(id);

        donations.forEach(d -> {
            d.cancel(reason);
            proposalRepository.cancelPendingProposals(d.getId(), ProposalCancelReason.TARGET_ENTITY_CANCELLED);
        });

        // Libera solicitudes donde participaba como voluntario
        List<HelpRequest> volunteeredRequests = helpRequestRepository.findByVolunteer_Id(id);

        volunteeredRequests.forEach(HelpRequest::releaseVolunteer);

        // Libera donaciones donde participaba como voluntario
        List<Donation> volunteeredDonations = donationRepository.findByVolunteer_Id(id);

        volunteeredDonations.forEach(Donation::releaseVolunteer);

        // Soft delete del usuario
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
    public UserResponseDto reactivateUser(UUID userId, UserCreateRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Actualizaa TODOS los campos del DTO
        user.setName(dto.getName());
        user.setEmail(dto.getEmail()); // Aunque el email sea el mismo, lo actualizamos
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        // Actualiza la ubicación
        if (dto.getLatitude() != null && dto.getLongitude() != null) {
            user.setLocation(dto.getLatitude(), dto.getLongitude());
        }

        // Reactiva
        user.setActive(true);
        user.setDeletedAt(null);

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }
}
