package com.communityhelp.app.config;

import com.communityhelp.app.user.model.Role;
import com.communityhelp.app.user.model.User;
import com.communityhelp.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración encargada de inicializar los datos básicos de la aplicación.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminDataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    /**
     * Crea automáticamente un usuario administrador al iniciar la aplicación
     * si no existe previamente.
     */
    @Bean
    CommandLineRunner createAdmin() {
        return args -> {

            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = User.builder()
                        .name("Admin")
                        .email(adminEmail)
                        .passwordHash(passwordEncoder.encode(adminPassword))
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(admin);
                log.warn("[config][AdminDataInitializer] Admin created");
            } else {
                log.info("[config][AdminDataInitializer] Admin already exists");
            }
        };
    }

}
