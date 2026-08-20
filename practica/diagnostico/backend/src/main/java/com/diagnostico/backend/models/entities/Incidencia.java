package com.diagnostico.backend.models.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.diagnostico.backend.exception.ReglaNegocioException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity // Indicamos que esta clase es una entidad de JPA, osea una Base de datos relacional
@Table(name = "incidencia") // Nos aseguramos de que la tabla se llame "incidencia" en la base de datos
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "id") 
    private Long id;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "descripcion", nullable = false, length = 2000)
    private String descripcion;

    @Enumerated(EnumType.STRING) // Indicamos que guardaremos el valor del enum como un String en la base de datos
    @Column(name = "categoria", nullable = false, length = 20)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 20)
    private Prioridad prioridad;

    // Guardamos el nivel de prioridad como un número entero, basicamente para poder ordenar las incidencias por prioridad de manera más sencilla
    @Column(name = "nivel_prioridad", nullable = false)
    private Integer nivelPrioridad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private Estado estado;

    @Column(name = "solicitante", nullable = false, length = 80)
    private String solicitante;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @OneToMany(mappedBy = "incidencia", cascade = CascadeType.ALL, orphanRemoval = true) 
    // Indicamos que la relación es bidireccional y que la entidad "CambioEstado" tiene una referencia a esta entidad a través del atributo incidencia
    // Indicamos que cuando eliminemos una incidencia, también se eliminarán todos los cambios de estado asociados a ella
    // Indicamos que cuando eliminemos un cambio de estado, también se eliminará de la lista de cambios de estado de la incidencia
    @OrderBy("fecha ASC, id ASC") // Cuando traiga la lista, sea ordenada
    private List<CambioEstado> historial = new ArrayList<>();

    // Constructor vacío requerido por JPA
    protected Incidencia() {
    }

    // Constructor privado para crear una nueva incidencia
    private Incidencia(String titulo, String descripcion, Categoria categoria, Prioridad prioridad, String solicitante) {
        LocalDateTime ahora = LocalDateTime.now();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.prioridad = prioridad;
        this.solicitante = solicitante;
        this.estado = Estado.inicial();
        this.fechaCreacion = ahora;
        this.fechaActualizacion = ahora;
    }

    // Metodo que hace que cada incidencia nueva sea ABIERTA y se agregue al historial de cambios de estado
    public static Incidencia registrar(String titulo, String descripcion, Categoria categoria, Prioridad prioridad, String solicitante) {
        Incidencia incidencia = new Incidencia(titulo, descripcion, categoria, prioridad, solicitante);
        incidencia.agregarAlHistorial(null, Estado.inicial(), "Incidencia registrada");
        return incidencia;
    }

    // Cuando la incidencia este en estado terminal, no se podra modificar
    public void actualizarDatos(String titulo, String descripcion, Categoria categoria, Prioridad prioridad, String solicitante) {
        if (estado.esTerminal()) {
            throw new ReglaNegocioException("La incidencia %d está %s y ya no admite modificaciones.".formatted(id, estado.getEtiqueta().toLowerCase()));
        }
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.prioridad = prioridad;
        this.solicitante = solicitante;
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Metodo que muestra los tipos de cambios de estado que se pueden hacer desde el estado actual de la incidencia
    public void cambiarEstado(Estado nuevoEstado, String comentario) {
        if (nuevoEstado == estado) {
            throw new ReglaNegocioException("La incidencia %d ya se encuentra en estado %s.".formatted(id, estado));
        }
        if (estado.esTerminal()) {
            throw new ReglaNegocioException("La incidencia %d está %s: es un estado final y no admite nuevos cambios.".formatted(id, estado.getEtiqueta().toLowerCase()));
        }
        if (!estado.permiteTransicionA(nuevoEstado)) {
            throw new ReglaNegocioException("Transición no permitida: %s -> %s. Desde %s solo se puede pasar a %s.".formatted(estado, nuevoEstado, estado, estado.transicionesPermitidas()));
        }
        Estado anterior = this.estado;
        this.estado = nuevoEstado;
        this.fechaActualizacion = LocalDateTime.now();
        this.fechaResolucion = nuevoEstado == Estado.RESUELTA ? this.fechaActualizacion : null;
        agregarAlHistorial(anterior, nuevoEstado, comentario);
    }

    // Metodo para agregar un cambio de estado al historial de la incidencia
    private void agregarAlHistorial(Estado anterior, Estado nuevo, String comentario) {
        CambioEstado cambio = new CambioEstado(this, anterior, nuevo, comentario);
        this.historial.add(cambio);
    }

    @PrePersist
    @PreUpdate
    // Antes de guardar o actualizar la incidencia, sincronizamos el nivel de prioridad con el valor del enum Prioridad
    // Asi nos aseguramos de que el nivel de prioridad siempre esté actualizado y sea consistente con la prioridad seleccionada
    private void sincronizarNivelPrioridad() {
        this.nivelPrioridad = prioridad.getNivel();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public Integer getNivelPrioridad() {
        return nivelPrioridad;
    }

    public Estado getEstado() {
        return estado;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public List<CambioEstado> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    @Override
    public boolean equals(Object otro) { // Hacemos que Java no sea tan literal y nos permita comparar objetos de manera más flexible
        if (this == otro) { // Revisamos si el otro objeto es la misma instancia que esta
            return true;
        }
        if (!(otro instanceof Incidencia incidencia)) { // Revisamos si el otro objeto es una instancia de Incidencia
            return false;
        }
        return id != null && id.equals(incidencia.id); // Comparamos los IDs de las incidencias, si ambos son nulos, consideramos que no son iguales
    }

    @Override
    public int hashCode() { // Verificamos que si dos incidencias son iguales, tengan el mismo hashcode, para que puedan ser usadas en colecciones como HashSet o HashMap
        return Objects.hashCode(id);
    }

}
