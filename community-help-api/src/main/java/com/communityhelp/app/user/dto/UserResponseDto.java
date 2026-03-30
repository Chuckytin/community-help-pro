package com.communityhelp.app.user.dto;

import com.communityhelp.app.user.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO que se enviará al frontend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private UUID id;
    private String name;
    private String email;
    private Role role;
    private Float rating;

    private boolean active;
    private boolean emailVerified;

    private Double latitude;
    private Double longitude;

}
