package com.diagnostico.backend.repository;

import com.diagnostico.backend.models.entities.Estado;
import com.diagnostico.backend.models.entities.Incidencia;
import com.diagnostico.backend.models.entities.Prioridad;
import com.diagnostico.backend.models.entities.Categoria;
import com.diagnostico.backend.models.entities.Orden;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

public final class IncidenciaSpecs {

    private IncidenciaSpecs() {
    }

    // Metodo que nos permite crear un filtro personalizado para poder buscar incidencias por estado, prioridad, categoria y titulo de la incidencia
    // root: representa la entidad Incidencia
    // consulta: representa la consulta que se va a ejecutar
    // cb: representa el CriteriaBuilder que nos permite construir la consulta de manera programatica
    public static Specification<Incidencia> filtro(Estado estado, Prioridad prioridad, Categoria categoria, String buscar) {
        return (root, consulta, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
        
            if (estado != null) {
                predicados.add(cb.equal(root.get("estado"), estado));
            }
            if (prioridad != null) {
                predicados.add(cb.equal(root.get("prioridad"), prioridad));
            }
            if (categoria != null) {
                predicados.add(cb.equal(root.get("categoria"), categoria));
            }
            if (buscar != null && !buscar.isBlank()) { // Si se cumplen las condiciones, se crea un filtro para buscar el titulo, descripcion y solicitante de la incidencia
                String patron = "%" + buscar.trim().toLowerCase() + "%"; // Se comparan en minusculas
                predicados.add(cb.or(
                    cb.like(cb.lower(root.<String>get("titulo")), patron),
                    cb.like(cb.lower(root.<String>get("descripcion")), patron),
                    cb.like(cb.lower(root.<String>get("solicitante")), patron)));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }

    // Metodo que nos permite ordenar las incidencias por fecha de creacion, titulo o prioridad
    public static Sort orden(Orden orden) {
        Orden criterio = orden == null ? Orden.RECIENTES : orden; // Si no se especifica un orden, se ordena por fecha de creacion descendente (de mas reciente a mas antigua)
        return switch (criterio) {
            case RECIENTES -> Sort.by(Sort.Direction.DESC, "fechaCreacion");
            case ANTIGUAS -> Sort.by(Sort.Direction.ASC, "fechaCreacion");
            case TITULO -> Sort.by(Sort.Direction.ASC, "titulo");
            case PRIORIDAD -> Sort.by(Sort.Order.desc("nivelPrioridad"), Sort.Order.asc("fechaCreacion"));
        };
    }
}
