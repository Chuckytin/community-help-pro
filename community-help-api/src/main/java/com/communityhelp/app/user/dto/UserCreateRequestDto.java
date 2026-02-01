package com.communityhelp.app.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que recibe el usuario del frontend para registrarse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 30, message = "Name must be between {min} and {max} characters")
    @Pattern(regexp = "^[\\w\\s-]+$", message = "Name can only contain letters, numbers, spaces and hyphens")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must have at least {min} characters")
    private String password; // passwordHash en el Service

    private Double latitude;
    private Double longitude;

}
