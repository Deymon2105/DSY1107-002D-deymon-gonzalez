package com.diagnostico.backend.dto;

public record ResumenResponse(
        long total,
        long abiertas,
        long enProgreso,
        long resueltas) {
}
