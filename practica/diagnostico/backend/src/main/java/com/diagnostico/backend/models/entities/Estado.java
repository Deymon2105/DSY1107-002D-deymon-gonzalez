package com.diagnostico.backend.models.entities;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;


public enum Estado { // Creamos 3 constantes, para los 3 estados que utilizaremos
    ABIERTA("Abierta"),
    EN_PROGRESO("En progreso"),
    RESUELTA("Resuelta");

    private static final Map<Estado, Set<Estado>> TRANSICIONES = Map.of( // Definimos las transiciones permitidas entre los estados, basicamente es un mapa de rutas
            ABIERTA, EnumSet.of(EN_PROGRESO, RESUELTA),
            EN_PROGRESO, EnumSet.of(ABIERTA, RESUELTA),
            RESUELTA, EnumSet.noneOf(Estado.class)
    );

    private final String etiqueta;

    // Constructor privado para inicializar la etiqueta de cada constante
    Estado(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    // Metodo para obtener la etiqueta de cada constante
    public String getEtiqueta() {
        return etiqueta;
    }

    // Metodo para obtener el estado inicial, que es ABIERTA
    public static Estado inicial() {
        return ABIERTA;
    }

    // Metodo que verifica si se puede hacer una transicion de estado desde el estado actual al estado destino
    public boolean permiteTransicionA(Estado destino) {
        return TRANSICIONES.get(this).contains(destino);
    }

    // Metodo que verifica si el estado actual es un estado terminal, es decir, que no permite transiciones a otros estados
    public boolean esTerminal() {
        return TRANSICIONES.get(this).isEmpty();
    }

    // Metodo que devuelve un conjunto de los estados a los que se puede transicionar desde el estado actual
    public Set<Estado> transicionesPermitidas() {
        return EnumSet.copyOf(TRANSICIONES.get(this));
    }
}
