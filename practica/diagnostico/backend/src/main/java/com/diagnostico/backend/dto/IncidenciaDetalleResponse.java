package com.diagnostico.backend.dto;

import java.util.List;

public record IncidenciaDetalleResponse(
        IncidenciaResponse incidencia,
        List<CambioEstadoResponse> historial) {
}
