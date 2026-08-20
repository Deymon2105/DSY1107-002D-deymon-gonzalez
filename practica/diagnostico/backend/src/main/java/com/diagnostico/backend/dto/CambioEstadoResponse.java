package com.diagnostico.backend.dto;

import com.diagnostico.backend.models.entities.Estado;
import java.time.LocalDateTime;

public record CambioEstadoResponse(
        Long id,
        Estado estadoAnterior,
        Estado estadoNuevo,
        String comentario,
        LocalDateTime fecha) {
}
