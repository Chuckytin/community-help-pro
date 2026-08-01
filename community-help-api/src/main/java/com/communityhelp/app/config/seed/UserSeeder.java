package com.communityhelp.app.config.seed;

import com.communityhelp.app.common.openroute.model.TransportMode;
import com.communityhelp.app.user.model.Role;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import com.communityhelp.app.volunteer.model.Volunteer;
import com.communityhelp.app.volunteer.model.VolunteerSkill;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Crea usuarios de prueba (algunos con perfil de voluntario) para desarrollo local.
 */
@Component
@Profile({"dev", "local"})
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Getter
    @Setter
    @NoArgsConstructor
    static class UserSeedDto {
        private String name;
        private String email;
        private String password;
        private Double latitude;
        private Double longitude;
        private VolunteerSeedDto volunteer;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    static class VolunteerSeedDto {
        private boolean available;
        private Double radiusKm;
        private String transportMode;
        private boolean emailNotificationsEnabled;
        private List<String> skills;
    }

    @Override
    public void run(String @NonNull ... args) throws Exception {

        Resource resource = resourceLoader.getResource("classpath:seed/users.json");

        List<UserSeedDto> seedDtos = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<>() {
                }
        );

        // Idempotencia
        if (!seedDtos.isEmpty()
                && userRepository.findByEmailIncludeInactive(seedDtos.getFirst().getEmail()).isPresent()) {
            log.info("Seeder omitido: los usuarios de prueba ya existen.");
            return;
        }

        int created = 0;
        for (UserSeedDto dto : seedDtos) {

            if (userRepository.findByEmailIncludeInactive(dto.getEmail()).isPresent()) {
                continue;
            }

            User user = User.builder()
                    .name(dto.getName())
                    .email(dto.getEmail())
                    .passwordHash(passwordEncoder.encode(dto.getPassword()))
                    .role(Role.USER)
                    .emailVerified(true)
                    .active(true)
                    .build();

            if (dto.getLatitude() != null && dto.getLongitude() != null) {
                user.setLocation(dto.getLatitude(), dto.getLongitude());
            }

            if (dto.getVolunteer() != null) {
                Volunteer volunteer = buildVolunteer(dto.getVolunteer(), user);
                user.setVolunteer(volunteer);
            }

            // cascade = ALL en User.volunteer propaga el guardado del Volunteer
            userRepository.save(user);
            created++;
        }

        log.info("Seeder completado: {} usuarios de prueba insertados.", created);
    }

    private Volunteer buildVolunteer(VolunteerSeedDto dto, User user) {
        Set<VolunteerSkill> skills = dto.getSkills() == null
                ? new HashSet<>()
                : dto.getSkills().stream()
                  .map(VolunteerSkill::valueOf)
                  .collect(Collectors.toSet());

        return Volunteer.builder()
                .user(user)
                .available(dto.isAvailable())
                .radiusKm(dto.getRadiusKm())
                .transportMode(TransportMode.valueOf(dto.getTransportMode()))
                .emailNotificationsEnabled(dto.isEmailNotificationsEnabled())
                .skills(skills)
                .build();
    }
}