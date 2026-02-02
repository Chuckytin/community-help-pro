package com.communityhelp.app.volunteer.model;

import com.communityhelp.app.common.persistence.Auditable;
import com.communityhelp.app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "volunteers")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Volunteer extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * User asociado al voluntario.
     * LAZY - el User asociado solo será cargado cuando se acceda explicitamente a él.
     * MapsId - el ID del volunteer compartirá el mismo valor que el del user.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @MapsId
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private boolean available = true;

    @Column(name = "radius_km")
    private Double radiusKm;

    /**
     * Habilidades del voluntario.
     * Mapea una colección de valores enumerados usando JPA,
     * cada habilidad se almacena en la tabla volunteer_skills vinculada a la columna volunteer_id.
     */
    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "volunteer_skills", joinColumns = @JoinColumn(name = "volunteer_id"))
    @Enumerated(EnumType.STRING)
    private Set<VolunteerSkill> skills = new HashSet<>();

    /**
     * Helper del User que devuelve su identificador.
     * Transient para que no forme parte del estado persistente de la entidad
     * (vive solo en memoria, no en la BBDD)
     */
    @Transient
    public UUID getUserId() {
        return user != null ? user.getId() : null;
    }

    /**
     * Helper del User que devuelve su nombre.
     * Transient para que no forme parte del estado persistente de la entidad
     * (vive solo en memoria, no en la BBDD)
     */
    @Transient
    public String getName() {
        return user != null ? user.getName() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Volunteer volunteer = (Volunteer) o;
        return Objects.equals(id, volunteer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
