package com.diagnostico.backend.dto;

import java.util.List;

public record CatalogosResponse(
        List<OpcionCatalogo> estados,
        List<OpcionCatalogo> prioridades,
        List<OpcionCatalogo> categorias,
        List<OpcionCatalogo> ordenamientos) {
}
