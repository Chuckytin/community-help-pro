package com.communityhelp.app.chat.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Información del mensaje que envía el usuario desde el frontend
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequestDto {

    /**
     * Contenido del mensaje enviado por el usuario.
     */
    @NotBlank
    @Size(max = 5000)
    private String content;
}
