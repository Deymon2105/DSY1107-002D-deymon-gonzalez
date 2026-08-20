package com.diagnostico.backend.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity // Indicamos que esta clase es una entidad de JPA, osea una Base de datos relacional
@Table(name = "cambio_estado") // Nos aseguramos de que la tabla se llame "cambio_estado" en la base de datos
public class CambioEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Con el fetch LAZY, la incidencia no se cargará toda, solo si se la pedimos
    @JoinColumn(name = "incidencia_id", nullable = false)
    private Incidencia incidencia;

    @Enumerated(EnumType.STRING) // Indicamos que guardaremos el valor del enum como un String en la base de datos
    @Column(name = "estado_anterior", length = 20)
    private Estado estadoAnterior;

    @Enumerated(EnumType.STRING) // Indicamos que guardaremos el valor del enum como un String en la base de datos
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private Estado estadoNuevo;

    @Column(name = "comentario", length = 255)
    private String comentario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    protected CambioEstado() {
    }

    // Constructor para crear un nuevo cambio de estado, se le pasa la incidencia, el estado anterior, el estado nuevo y un comentario opcional
    CambioEstado(Incidencia incidencia, Estado estadoAnterior, Estado estadoNuevo, String comentario) {
        this.incidencia = incidencia;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.comentario = (comentario == null || comentario.isBlank()) ? null : comentario.trim(); // Nos aseguramos de que el comentario no sea nulo ni esté vacío, si lo está, lo dejamos como null
        this.fecha = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Incidencia getIncidencia() {
        return incidencia;
    }

    public Estado getEstadoAnterior() {
        return estadoAnterior;
    }

    public Estado getEstadoNuevo() {
        return estadoNuevo;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
