package com.diagnostico.backend.models.entities;

public enum Categoria { // Creamos 5 constantes, para las 5 categorias que utilizaremos
    ACCESOS("Accesos y cuentas"),
    HARDWARE("Hardware y equipos"),
    SOFTWARE("Software y aplicaciones"),
    RED("Red y conectividad"),
    OTRO("Otro");

    private final String etiqueta;

    // Constructor privado para inicializar la etiqueta de cada constante
    Categoria(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    // Metodo para obtener la etiqueta de cada constante
    public String getEtiqueta() {
        return etiqueta;
    }
}
