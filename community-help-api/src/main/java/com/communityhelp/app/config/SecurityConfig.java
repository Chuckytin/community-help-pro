package com.communityhelp.app.config;

import com.communityhelp.app.auth.oauth2.OAuth2FailureHandler;
import com.communityhelp.app.auth.oauth2.OAuth2SuccessHandler;
import com.communityhelp.app.auth.service.JwtService;
import com.communityhelp.app.auth.service.TokenBlacklistService;
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

    private static final String[] SWAGGER = {
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };

    /**
     * Configuración de la cadena de filtros HTTP.
     * - Desactiva CSRF y configura CORS según CorsConfig
     * - Sesiones sin estado (IF_REQUIRED, ya que OAuth2 necesita sesión temporal durante el intercambio con Google)
     * - Filtros: rate limiting y validación JWT, ambos antes de UsernamePasswordAuthenticationFilter
     * - Login público (auth, Swagger); WebSocket y el resto de endpoints requieren autenticación
     * - OAuth2 con Google: OAuth2SuccessHandler genera el JWT tras login correcto, OAuth2FailureHandler redirige al frontend si falla
     * - Excepciones gestionadas con CustomAuthenticationEntryPoint y CustomAccessDeniedHandler
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitFilter rateLimitFilter,
            CustomAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler,
            OAuth2FailureHandler oAuth2FailureHandler
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
                                SWAGGER
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                        .requestMatchers("/ws/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean del filtro JWT.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            SecurityResponseWriter securityResponseWriter,
            TokenBlacklistService tokenBlacklistService
    ) {
        return new JwtAuthenticationFilter(jwtService, securityResponseWriter, tokenBlacklistService);
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
