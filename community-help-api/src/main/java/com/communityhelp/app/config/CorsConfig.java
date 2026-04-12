package com.communityhelp.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing).
 * Permite que el frontend haga peticiones al backend sin ser bloqueado por el navegador.
 */
@Configuration
public class CorsConfig {

    @Value("${url.frontend}")
    private String frontendUrl;

    /**
     * Define qué orígenes, métodos y headers están permitidos.
     * Se aplica a todas las rutas de la API.
     * - allowedOrigins: Solo permite peticiones desde el frontend (http://localhost:5173).
     * - allowedMethods: Permite los métodos HTTP comunes (GET, POST, PUT, PATCH, DELETE, OPTIONS).
     * - allowedHeaders: Permite cualquier header (necesario para Authorization con JWT).
     * - allowCredentials: Permite enviar cookies o headers de autenticación.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(frontendUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}