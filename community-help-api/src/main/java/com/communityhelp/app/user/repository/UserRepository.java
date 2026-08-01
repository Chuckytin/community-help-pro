package com.communityhelp.app.user.repository;

import com.communityhelp.app.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Obtiene un usuario ACTIVO por su email (login/autenticación).
     * Excluye explícitamente usuarios soft-deleted.
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.active = true")
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * Incluye usuarios inactivos — usado en flujos de reactivación (registro y OAuth2).
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailIncludeInactive(@Param("email") String email);

    /**
     * Actualiza el rating del usuario por las reviews.
     * Evita problemas de caché del persistence context
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE User u SET u.rating = :rating WHERE u.id = :userId")
    void updateRating(@Param("userId") UUID userId,
                      @Param("rating") Float rating);

    /**
     * Encuentra usuarios que no han verificado su email y fueron creados antes de una fecha dada.
     */
    List<User> findByEmailVerifiedFalseAndCreatedAtBefore(LocalDateTime cutoff);

}
