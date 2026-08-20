package com.diagnostico.backend.dto;

import com.diagnostico.backend.models.entities.Categoria;
import com.diagnostico.backend.models.entities.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ActualizarIncidenciaRequest(

        @NotBlank(message = "El título es obligatorio.")
        @Size(min = 5, max = 120, message = "El título debe tener entre 5 y 120 caracteres.")
        String titulo,

        @NotBlank(message = "La descripción es obligatoria.")
        @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres.")
        String descripcion,

        @NotNull(message = "La categoría es obligatoria.")
        Categoria categoria,

        @NotNull(message = "La prioridad es obligatoria.")
        Prioridad prioridad,

        @NotBlank(message = "El solicitante es obligatorio.")
        @Size(min = 3, max = 80, message = "El solicitante debe tener entre 3 y 80 caracteres.")
        String solicitante) {
}
