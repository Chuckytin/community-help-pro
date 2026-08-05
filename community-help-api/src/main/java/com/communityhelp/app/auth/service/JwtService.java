package com.communityhelp.app.auth.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;

public interface JwtService {

    UserDetails authenticate(String email, String password);

    String generateToken(UserDetails userDetails);

    UserDetails validateToken(String token);

    long getJwtExpiryMs();

    Instant getExpiration(String token);

}
