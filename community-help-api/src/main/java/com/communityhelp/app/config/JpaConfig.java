package com.communityhelp.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Clase Configuration para el JPA auditing,
 * permite que los campos createdAt y updatedAt se rellenen automáticamente.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
