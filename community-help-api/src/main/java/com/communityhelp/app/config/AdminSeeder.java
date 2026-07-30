package com.communityhelp.app.config;

import com.communityhelp.app.user.model.Role;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea automáticamente un usuario administrador al iniciar la aplicación,
 * si no existe previamente.
 */
@Component
@Profile({"local", "dev", "prod"})
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String @NonNull ... args) {

        try {
            if (userRepository.findByEmail(adminEmail).isPresent()) {
                log.info("Seeder omitido: el usuario admin ya existe.");
                return;
            }

            User admin = User.builder()
                    .name("Admin")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .emailVerified(true)
                    .build();

            userRepository.save(admin);
            log.info("Seeder completado: usuario admin creado con email {}.", adminEmail);

        } catch (Exception e) {
            log.error("Seeder falló — la tabla users puede no existir aún: {}", e.getMessage());
            throw e;
        }
    }
}