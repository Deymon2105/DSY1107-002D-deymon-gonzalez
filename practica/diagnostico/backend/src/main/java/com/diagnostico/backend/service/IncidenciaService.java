package com.diagnostico.backend.service;

import com.diagnostico.backend.models.entities.Categoria;
import com.diagnostico.backend.models.entities.Estado;
import com.diagnostico.backend.models.entities.Incidencia;
import com.diagnostico.backend.models.entities.Orden;
import com.diagnostico.backend.models.entities.Prioridad;
import com.diagnostico.backend.dto.ActualizarIncidenciaRequest;
import com.diagnostico.backend.dto.CambiarEstadoRequest;
import com.diagnostico.backend.dto.CrearIncidenciaRequest;
import com.diagnostico.backend.dto.IncidenciaDetalleResponse;
import com.diagnostico.backend.dto.IncidenciaResponse;
import com.diagnostico.backend.dto.ResumenResponse;
import com.diagnostico.backend.exception.RecursoNoEncontradoException;
import com.diagnostico.backend.mapper.IncidenciaMapper;
import com.diagnostico.backend.repository.IncidenciaRepository;
import com.diagnostico.backend.repository.IncidenciaSpecs;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class IncidenciaService {

    private static final Logger log = LoggerFactory.getLogger(IncidenciaService.class);

    private final IncidenciaRepository repositorio; // Repositorio de incidencias
    private final IncidenciaMapper mapper; // Mapper para convertir entre entidades y DTOs

    public IncidenciaService(IncidenciaRepository repositorio, IncidenciaMapper mapper) { // Constructor que inyecta el repositorio y el mapper
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    // Transactional sirve por si se produce un error en la transaccion, se deshacen los cambios realizados en la base de datos

    @Transactional(readOnly = true) // Utilizamos el readOnly = true para indicar que no se van a realizar cambios en la base de datos
    public List<IncidenciaResponse> listar(Estado estado, Prioridad prioridad, Categoria categoria, String buscar, Orden orden) { // Metodo para listar las incidencias, se pueden filtrar por estado, prioridad, categoria y titulo de la incidencia, ademas se puede ordenar por fecha de creacion, titulo o prioridad
        return repositorio
                .findAll(IncidenciaSpecs.filtro(estado, prioridad, categoria, buscar), IncidenciaSpecs.orden(orden))
                .stream()
                .map(mapper::aRespuesta)
                .toList();
    }

    @Transactional(readOnly = true) 
    public IncidenciaDetalleResponse obtener(Long id) { // Metodo para obtener una incidencia por su id, primero se busca la incidencia por su id, si no existe, se lanza una excepcion
        Incidencia incidencia = buscarOFallar(id);
        return mapper.aDetalle(incidencia);
    }

    @Transactional // Utilizamos la anotacion @Transactional para indicar que este metodo realiza cambios en la base de datos 
    public IncidenciaDetalleResponse crear(CrearIncidenciaRequest peticion) { // Metodo para crear una incidencia, primero se valida que los datos sean correctos, luego se crea la incidencia y se guarda en la base de datos
        Incidencia incidencia = Incidencia.registrar(
                peticion.titulo().trim(),
                peticion.descripcion().trim(),
                peticion.categoria(),
                peticion.prioridad(),
                peticion.solicitante().trim());

        Incidencia guardada = repositorio.save(incidencia);
        log.info("Incidencia {} registrada en estado {}", guardada.getId(), guardada.getEstado());
        return mapper.aDetalle(guardada);
    }

    @Transactional
    public IncidenciaDetalleResponse actualizar(Long id, ActualizarIncidenciaRequest peticion) { // Metodo para actualizar una incidencia, primero se busca la incidencia por su id, si no existe, se lanza una excepcion
        Incidencia incidencia = buscarOFallar(id);
        incidencia.actualizarDatos(
                peticion.titulo().trim(),
                peticion.descripcion().trim(),
                peticion.categoria(),
                peticion.prioridad(),
                peticion.solicitante().trim());

        log.info("Incidencia {} actualizada", id);
        return mapper.aDetalle(incidencia);
    }

    @Transactional
    public IncidenciaDetalleResponse cambiarEstado(Long id, CambiarEstadoRequest peticion) { // Metodo para cambiar el estado de una incidencia, primero se busca la incidencia por su id, si no existe, se lanza una excepcion
        Incidencia incidencia = buscarOFallar(id);
        Estado anterior = incidencia.getEstado();
        incidencia.cambiarEstado(peticion.nuevoEstado(), peticion.comentario());

        log.info("Incidencia {}: {} -> {}", id, anterior, peticion.nuevoEstado());
        return mapper.aDetalle(incidencia);
    }

    @Transactional
    public void eliminar(Long id) { // Metodo para eliminar una incidencia, primero se busca la incidencia por su id, si no existe, se lanza una excepcion
        Incidencia incidencia = buscarOFallar(id);
        repositorio.delete(incidencia);
        log.info("Incidencia {} eliminada", id);
    }

    @Transactional(readOnly = true)
    public ResumenResponse resumen() { // Metodo que nos permite obtener un resumen de la cantidad de incidencias por estado
        long abiertas = repositorio.countByEstado(Estado.ABIERTA);
        long enProgreso = repositorio.countByEstado(Estado.EN_PROGRESO);
        long resueltas = repositorio.countByEstado(Estado.RESUELTA);
        return new ResumenResponse(abiertas + enProgreso + resueltas, abiertas, enProgreso, resueltas);
    }

    private Incidencia buscarOFallar(Long id) { // Se creo con el fin de no repetir el mismo codigo en varios metodos, ya que se utiliza para buscar una incidencia por su id y si no existe, lanzar una excepcion
        return repositorio.findById(id)
                .orElseThrow(() -> RecursoNoEncontradoException.incidencia(id));
    }
}
