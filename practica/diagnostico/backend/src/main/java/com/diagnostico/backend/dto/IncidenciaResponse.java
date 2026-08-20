package com.diagnostico.backend.dto;

import com.diagnostico.backend.models.entities.Categoria;
import com.diagnostico.backend.models.entities.Estado;
import com.diagnostico.backend.models.entities.Prioridad;
import java.time.LocalDateTime;
import java.util.Set;

public record IncidenciaResponse(
        Long id,
        String titulo,
        String descripcion,
        Categoria categoria,
        Prioridad prioridad,
        Estado estado,
        String solicitante,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        LocalDateTime fechaResolucion,
        Set<Estado> transicionesPermitidas) {
}
