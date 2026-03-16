package com.communityhelp.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita ejecución asíncrona en el sistema.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}