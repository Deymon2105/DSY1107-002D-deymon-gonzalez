package com.diagnostico.backend.models.entities;

public enum Orden { // Creamos 4 constantes, para los 4 tipos de ordenamiento que utilizaremos
    RECIENTES("Más recientes primero"),
    ANTIGUAS("Más antiguas primero"),
    PRIORIDAD("Mayor prioridad primero"),
    TITULO("Título A-Z");

    private final String etiqueta;

    // Constructor privado para inicializar la etiqueta de cada constante
    Orden(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    // Metodo para obtener la etiqueta de cada constante
    public String getEtiqueta() {
        return etiqueta;
    }
}
