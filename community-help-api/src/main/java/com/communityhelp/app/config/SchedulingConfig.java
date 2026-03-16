package com.communityhelp.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita las anotaciones @Scheduled para tareas programadas.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
