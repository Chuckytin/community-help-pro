package com.communityhelp.app.auth.service;

import com.communityhelp.app.security.AppUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementación del servicio de autenticación.
 * - Autentica al usuario mediante el email y contraseña.
 * - Genera tokens JWT.
 * - Valida tokens JWT y obtiene los datos del usuario.
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private long jwtExpiryMs;

    /**
     * Auténtica al usuario con email y contraseña.
     * - Llama al AuthenticationManager para validar las credenciales.
     * - Devuelve los detalles del usuario para generar el JWT.
     */
    @Override
    public UserDetails authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        return userDetailsService.loadUserByUsername(email);
    }

    /**
     * Genera un token JWT para un usuario autenticado.
     * - Incluye el email como subject y otros claims como userId y role.
     * - Firma el token con HMAC SHA-256 usando la clave secreta.
     * - Establece la fecha de emisión y expiración del token.
     */
    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        if (userDetails instanceof AppUserDetails appUser) {
            claims.put("userId", appUser.getId().toString());
            claims.put("role", appUser.getRole().name());
        }

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiryMs))
                .signWith(getSignKey())
                .compact();
    }

    /**
     * Valida el token JWT y obtiene el usuario asociado.
     */
    @Override
    public UserDetails validateToken(String token) {
        String username = extractUsername(token);
        return userDetailsService.loadUserByUsername(username);
    }

    /**
     * Devuelve el tiempo de expiración del JWT en milisegundos.
     */
    @Override
    public long getJwtExpiryMs() {
        return jwtExpiryMs;
    }

    /**
     * Extrae el username (email) desde el token JWT.
     */
    private String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Crea la clave secreta para firmar el token JWT (firma HMAC SHA-256)
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

}
