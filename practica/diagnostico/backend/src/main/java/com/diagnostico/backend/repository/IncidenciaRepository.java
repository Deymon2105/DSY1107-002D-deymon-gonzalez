package com.diagnostico.backend.repository;

import com.diagnostico.backend.models.entities.Estado;
import com.diagnostico.backend.models.entities.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface IncidenciaRepository // Aqui utilizamos la interfaz JpaRepository para poder realizar operaciones CRUD sobre la entidad Incidencia
    extends JpaRepository<Incidencia, Long>, JpaSpecificationExecutor<Incidencia> { // Con JpaSpecificationExecutor le decimos que vamos a utilizar filtros personalizados para poder realizar busquedas mas complejas, como por ejemplo buscar por estado, prioridad, categoria y titulo de la incidencia

    long countByEstado(Estado estado); // Este metodo nos permite contar la cantidad de incidencias que tienen un determinado estado, por ejemplo, cuantas incidencias estan en estado "ABIERTA" o "CERRADA"
}
