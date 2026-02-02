package com.communityhelp.app.user.model;

import com.communityhelp.app.common.persistence.AuditableLocatable;
import com.communityhelp.app.helprequest.model.HelpRequest;
import com.communityhelp.app.volunteer.model.Volunteer;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class User extends AuditableLocatable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private Float rating;

    /**
     * Información del voluntario asociada al usuario.
     * LAZY - se carga solo cuando se accede a él.
     * All - las operaciones de persistencia se propagan al Volunteer.
     * orphanRemoval - si se desvincula o se elimina el User, el Volunteer se elimina de la BBDD.
     */
    @OneToOne(
            mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Volunteer volunteer;

    /**
     * Solicitudes de ayuda asociadas al usuario.
     * LAZY - se carga solo cuando se accede a él.
     * All - las operaciones de persistencia se propagan al Volunteer.
     * orphanRemoval - si se desvincula o se elimina el User, el Volunteer se elimina de la BBDD.
     */
    @OneToMany(
            mappedBy = "requester",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<HelpRequest> helpRequests = new HashSet<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
