package com.diagnostico.backend.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public static RecursoNoEncontradoException incidencia(Long id) {
        return new RecursoNoEncontradoException("No existe una incidencia con id " + id + ".");
    }
}
