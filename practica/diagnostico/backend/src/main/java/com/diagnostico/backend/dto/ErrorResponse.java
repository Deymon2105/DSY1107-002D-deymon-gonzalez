package com.diagnostico.backend.dto;

import java.time.LocalDateTime;
import java.util.List;


public record ErrorResponse(
        LocalDateTime marcaTiempo,
        int estado,
        String error,
        String mensaje,
        String ruta,
        List<DetalleCampo> detalles) {

    public static ErrorResponse de(int estado, String error, String mensaje, String ruta) {
        return new ErrorResponse(LocalDateTime.now(), estado, error, mensaje, ruta, List.of());
    }

    public static ErrorResponse de(int estado, String error, String mensaje, String ruta, List<DetalleCampo> detalles) {
        return new ErrorResponse(LocalDateTime.now(), estado, error, mensaje, ruta, detalles);
    }
}
