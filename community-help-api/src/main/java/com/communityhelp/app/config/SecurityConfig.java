package com.communityhelp.app.config;

import com.communityhelp.app.auth.service.AuthenticationService;
import com.communityhelp.app.security.AppUserDetailsService;
import com.communityhelp.app.security.JwtAuthenticationFilter;
import com.communityhelp.app.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración principal de Spring Security.
 * - Beans para la autenticación (AuthenticationManager, PasswordEncoder).
 * - UserDetailsService personalizado.
 * - Filtro JWT y su integración en la cadena de filtros.
 * - Reglas de acceso a los endpoints.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configuración de la cadena de filtros HTTP.
     * - Permite login público
     * - Requiere autenticación para otros endpoints privados
     * - Desactiva CSRF
     * - Usa sesión STATELESS
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // TODO
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/me").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/volunteers/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/volunteers/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/volunteers/me").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean del filtro JWT.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationService authenticationService) {
        return new JwtAuthenticationFilter(authenticationService);
    }

    /**
     * Bean de UserDetailsService basado en la BBDD.
     */
    @Bean
    public AppUserDetailsService userDetailsService(UserRepository userRepository) {
        return new AppUserDetailsService(userRepository);
    }

    /**
     * Bean de codificación de contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean de AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
