package com.communityhelp.app.security;

import com.communityhelp.app.user.model.Role;
import com.communityhelp.app.user.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Adaptación del User para Spring Security.
 * - Permite usar un usuario autenticado con JWT y Spring Security.
 * - Se expone el email como username y se mapean los roles dinámicamente.
 */
@Getter
@RequiredArgsConstructor
public class AppUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        // Para manejar múltiples roles:
        // return user.getRoles().stream()
        //         .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
        //         .collect(Collectors.toList());
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Devuelve el UUID del usuario.
     */
    public UUID getId() {
        return user.getId();
    }

    /**
     * Devuelve el rol del usuario como enum.
     */
    public Role getRole() {
        return user.getRole();
    }

}
