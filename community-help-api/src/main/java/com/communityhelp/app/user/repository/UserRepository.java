package com.communityhelp.app.user.repository;

import com.communityhelp.app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * Método para incluir usuarios inactivos (soft delete)
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailIncludeInactive(@Param("email") String email);
}
