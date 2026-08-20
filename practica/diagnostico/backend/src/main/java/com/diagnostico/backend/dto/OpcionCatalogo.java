package com.diagnostico.backend.dto;

public record OpcionCatalogo(
        String codigo,
        String etiqueta,
        Integer nivel) 
        {

    public static OpcionCatalogo de(String codigo, String etiqueta) {
        return new OpcionCatalogo(codigo, etiqueta, null);
    }
}
