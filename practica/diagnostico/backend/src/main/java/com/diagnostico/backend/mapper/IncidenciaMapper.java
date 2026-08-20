package com.diagnostico.backend.mapper;

import com.diagnostico.backend.models.entities.CambioEstado;
import com.diagnostico.backend.models.entities.Incidencia;
import com.diagnostico.backend.dto.CambioEstadoResponse;
import com.diagnostico.backend.dto.IncidenciaDetalleResponse;
import com.diagnostico.backend.dto.IncidenciaResponse;
import java.util.List;
import org.springframework.stereotype.Component;


@Component
public class IncidenciaMapper {

    public IncidenciaResponse aRespuesta(Incidencia incidencia) { // Metodo que convierte la entidad de Incidencia a un DTO de IncidenciaResponse, que contiene la informacion basica de la incidencia, incluyendo el estado actual y las transiciones permitidas desde ese estado
        return new IncidenciaResponse(
                incidencia.getId(),
                incidencia.getTitulo(),
                incidencia.getDescripcion(),
                incidencia.getCategoria(),
                incidencia.getPrioridad(),
                incidencia.getEstado(),
                incidencia.getSolicitante(),
                incidencia.getFechaCreacion(),
                incidencia.getFechaActualizacion(),
                incidencia.getFechaResolucion(),
                incidencia.getEstado().transicionesPermitidas());
    }

    public IncidenciaDetalleResponse aDetalle(Incidencia incidencia) { // Metodo que convierte la entidad de Incidencia a un DTO de IncidenciaDetalleResponse, que contiene la informacion completa de la incidencia, incluyendo el historial de cambios de estado
        List<CambioEstadoResponse> historial = incidencia.getHistorial().stream()
                .map(this::aRespuesta)
                .toList();
        return new IncidenciaDetalleResponse(aRespuesta(incidencia), historial);
    }

    public CambioEstadoResponse aRespuesta(CambioEstado cambio) { // Metodo que convierte la entidad de CambioEstado a un DTO de CambioEstadoResponse, que contiene la informacion de un cambio de estado en el historial de la incidencia
        return new CambioEstadoResponse(
                cambio.getId(),
                cambio.getEstadoAnterior(),
                cambio.getEstadoNuevo(),
                cambio.getComentario(),
                cambio.getFecha());
    }
}
