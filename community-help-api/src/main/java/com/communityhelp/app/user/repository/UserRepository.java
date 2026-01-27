package com.communityhelp.app.user.repository;

import com.communityhelp.app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Obtiene un usuario por su email (para login/autenticación).
     */
    Optional<User> findByEmail(String email);

    /**
     * Comprueba si existe un usuario con un email determinado.
     */
    boolean existsByEmail(String email);

}
