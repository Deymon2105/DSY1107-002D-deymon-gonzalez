package com.diagnostico.backend.dto;

import com.diagnostico.backend.models.entities.Estado;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CambiarEstadoRequest(

        @NotNull(message = "El nuevo estado es obligatorio.")
        Estado nuevoEstado,

        @Size(max = 255, message = "El comentario no puede superar los 255 caracteres.")
        String comentario) {
}

