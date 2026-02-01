package com.communityhelp.app.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Por si el usuario quiere actualizar sus datos de perfil
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {

    //@NotBlank no se usa porque los campos son opcionales
    @Size(min = 2, max = 30, message = "Name must be between {min} and {max} characters")
    @Pattern(regexp = "^[\\w\\s-]+$", message = "Name can only contain letters, numbers, spaces and hyphens")
    private String name;

    @Email(message = "Email is not valid")
    private String email;

    @Size(min = 6, message = "Password must have at least {min} characters")
    private String password;

    private Double latitude;
    private Double longitude;

}
