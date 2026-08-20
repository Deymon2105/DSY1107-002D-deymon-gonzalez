package com.diagnostico.backend.controller;

import com.diagnostico.backend.models.entities.Categoria;
import com.diagnostico.backend.models.entities.Estado;
import com.diagnostico.backend.models.entities.Orden;
import com.diagnostico.backend.models.entities.Prioridad;
import com.diagnostico.backend.dto.ActualizarIncidenciaRequest;
import com.diagnostico.backend.dto.CambiarEstadoRequest;
import com.diagnostico.backend.dto.CrearIncidenciaRequest;
import com.diagnostico.backend.dto.IncidenciaDetalleResponse;
import com.diagnostico.backend.dto.IncidenciaResponse;
import com.diagnostico.backend.dto.ResumenResponse;
import com.diagnostico.backend.service.IncidenciaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController // RequestController indica que esta clase es un controlador de Spring Boot, y que los metodos de esta clase van a manejar las solicitudes HTTP y devolver respuestas HTTP
@RequestMapping("/api/incidencias") // Aqui definimos la ruta base para todos los endpoints de este controlador
public class IncidenciaController {

    private final IncidenciaService servicio;

    public IncidenciaController(IncidenciaService servicio) { // Constructor que inyecta el servicio de incidencias, que es el encargado de manejar la logica de negocio y comunicarse con el repositorio
        this.servicio = servicio;
    }


    @GetMapping // GetMapping indica que este metodo va a manejar las solicitudes HTTP GET, y que la ruta para este endpoint es la ruta base definida en la clase
    public List<IncidenciaResponse> listar( // Metodo que nos permite listar las incidencias, se pueden filtrar por estado, prioridad, categoria y titulo de la incidencia, ademas se puede ordenar por fecha de creacion, titulo o prioridad
            @RequestParam(required = false) Estado estado,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Orden orden) {
        return servicio.listar(estado, prioridad, categoria, buscar, orden);
    }

    @GetMapping("/resumen") // Se agrega la ruta /resumen para poder obtener un resumen de las incidencias
    public ResumenResponse resumen() { // Metodo que nos permite obtener un resumen de las incidencias, como por ejemplo la cantidad de incidencias abiertas, en progreso y resueltas
        return servicio.resumen();
    }

    @GetMapping("/{id}") // Se agrega la ruta /{id} para poder obtener una incidencia por su id
    public IncidenciaDetalleResponse obtener(@PathVariable Long id) { // Metodo que nos permite obtener una incidencia por su id, primero se busca la incidencia por su id, si no existe, se lanza una excepcion
        return servicio.obtener(id);
    }

    @PostMapping // PostMapping nos permite crear una nueva incidencia, y la ruta para este endpoint es la ruta base definida en la clase
    public ResponseEntity<IncidenciaDetalleResponse> crear(@Valid @RequestBody CrearIncidenciaRequest peticion) { // Metodo que nos permite crear una incidencia, primero se valida que los datos sean correctos, luego se crea la incidencia y se guarda en la base de datos
        IncidenciaDetalleResponse creada = servicio.crear(peticion);
        URI ubicacion = URI.create("/api/incidencias/" + creada.incidencia().id());
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{id}") // Se agrega la ruta /{id} para poder actualizar una incidencia por su id
    public IncidenciaDetalleResponse actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarIncidenciaRequest peticion) { // Metodo que nos permite actualizar una incidencia por su id, primero se busca la incidencia por su id, si no existe, se lanza una excepcion, luego se actualiza la incidencia y se guarda en la base de datos
        return servicio.actualizar(id, peticion);
    }

    @PatchMapping("/{id}/estado") // Se agrega la ruta /{id}/estado para poder cambiar el estado de una incidencia por su id
    public IncidenciaDetalleResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest peticion) { // Metodo que nos permite cambiar el estado de una incidencia por su id, primero se busca la incidencia por su id, si no existe, se lanza una excepcion, luego se cambia el estado de la incidencia y se guarda en la base de datos
        return servicio.cambiarEstado(id, peticion);
    }

    @DeleteMapping("/{id}") // Se agrega la ruta /{id} para poder eliminar una incidencia por su id
    @ResponseStatus(HttpStatus.NO_CONTENT) // Utilizamos ResponseStatus para indicar que la respuesta de este endpoint es 204 No Content, ya que no se devuelve ningun contenido en la respuesta
    public void eliminar(@PathVariable Long id) { // Metodo que nos permite eliminar una incidencia por su id, primero se busca la incidencia por su id, si no existe, se lanza una excepcion, luego se elimina la incidencia de la base de datos
        servicio.eliminar(id);
    }
}
