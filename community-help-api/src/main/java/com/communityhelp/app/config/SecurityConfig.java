package com.communityhelp.app.config;

import com.communityhelp.app.auth.oauth2.OAuth2SuccessHandler;
import com.communityhelp.app.auth.service.AuthenticationService;
import com.communityhelp.app.security.*;
import com.communityhelp.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Configuración principal de Spring Security.
 * - Beans para la autenticación (AuthenticationManager, PasswordEncoder).
 * - UserDetailsService personalizado.
 * - Filtro JWT y su integración en la cadena de filtros.
 * - Reglas de acceso a los endpoints.
 * EnableMethodSecurity - activa la seguridad a nivel de métodos con anotaciones como @PreAuthorize
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    /**
     * Configuración de la cadena de filtros HTTP.
     * - Permite login público
     * - Requiere autenticación para otros endpoints privados
     * - WebSocker requiere autenticación JWT
     * - Desactiva CSRF
     * - Configura CORS con la configuración definida en CorsConfig
     * - Agrega el filtro de limitación de tasa antes del filtro de autenticación para proteger contra ataques de fuerza bruta
     * - Agrega el filtro JWT antes del filtro de autenticación para validar el token en cada request
     * - Configura el manejo de excepciones para devolver respuestas adecuadas en caso de autenticación fallida o acceso denegado
     * - Configura la política de creación de sesiones para que Spring Security no cree sesiones innecesarias (usamos JWT)
     * - Configura la autenticación OAuth2 para que use el OAuth2SuccessHandler personalizado que genera el JWT después de la autenticación con Google
     * - Configura las reglas de autorización para permitir el acceso público a ciertos endpoints (login, Swagger) y requerir autenticación para otros (API, WebSocket)
     * - Cualquier request que no coincida con las reglas anteriores requerirá autenticación.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitFilter rateLimitFilter,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler
    ) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/chat-test.html",
                                "/error",
                                "/favicon.ico",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                        .requestMatchers("/ws/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
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
