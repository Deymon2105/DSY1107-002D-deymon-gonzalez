package com.diagnostico.backend.models.entities;

public enum Prioridad { // Creamos 3 constantes, para los 3 niveles de prioridad que utilizaremos
    BAJA("Baja", 1),
    MEDIA("Media", 2),
    ALTA("Alta", 3);

    private final String etiqueta;
    private final int nivel;

    // Constructor privado para inicializar la etiqueta y el nivel de cada constante
    Prioridad(String etiqueta, int nivel) {
        this.etiqueta = etiqueta;
        this.nivel = nivel;
    }

    // Metodo para obtener la etiqueta de cada constante
    public String getEtiqueta() {
        return etiqueta;
    }

    // Metodo para obtener el nivel de cada constante
    public int getNivel() {
        return nivel;
    }
}
