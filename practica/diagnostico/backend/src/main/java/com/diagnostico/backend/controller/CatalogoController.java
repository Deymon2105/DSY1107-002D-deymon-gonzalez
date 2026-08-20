package com.diagnostico.backend.controller;

import com.diagnostico.backend.models.entities.Categoria;
import com.diagnostico.backend.models.entities.Estado;
import com.diagnostico.backend.models.entities.Orden;
import com.diagnostico.backend.models.entities.Prioridad;
import com.diagnostico.backend.dto.CatalogosResponse;
import com.diagnostico.backend.dto.OpcionCatalogo;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController // RequestController indica que esta clase es un controlador de Spring Boot, y que los metodos de esta clase van a manejar las solicitudes HTTP y devolver respuestas HTTP
@RequestMapping("/api/catalogos") // Aqui definimos la ruta base para todos los endpoints de este controlador
public class CatalogoController {

    @GetMapping // GetMapping indica que este metodo va a manejar las solicitudes HTTP GET, y que la ruta para este endpoint es la ruta base definida en la clase
    public CatalogosResponse catalogos() { // Metodo que nos permite obtener todos los catalogos de la aplicacion, como por ejemplo los estados, prioridades, categorias y ordenamientos
        return new CatalogosResponse(estados(), prioridades(), categorias(), ordenamientos());
    }

    private List<OpcionCatalogo> estados() { // Metodo que nos permite obtener todos los estados de la aplicacion, como por ejemplo "ABIERTA", "EN PROGRESO" y "RESUELTA"
        return Arrays.stream(Estado.values())
                .map(estado -> OpcionCatalogo.de(estado.name(), estado.getEtiqueta()))
                .toList();
    }

    private List<OpcionCatalogo> prioridades() { // Metodo que nos permite obtener todas las prioridades de la aplicacion, como por ejemplo "BAJA", "MEDIA" y "ALTA"
        return Arrays.stream(Prioridad.values())
                .map(prioridad -> new OpcionCatalogo(
                        prioridad.name(), prioridad.getEtiqueta(), prioridad.getNivel()))
                .toList();
    }

    private List<OpcionCatalogo> categorias() { // Metodo que nos permite obtener todas las categorias de la aplicacion, como por ejemplo "SOFTWARE", "HARDWARE" y "REDES"
        return Arrays.stream(Categoria.values())
                .map(categoria -> OpcionCatalogo.de(categoria.name(), categoria.getEtiqueta()))
                .toList();
    }

    private List<OpcionCatalogo> ordenamientos() { // Metodo que nos permite obtener todos los ordenamientos de la aplicacion, como por ejemplo "FECHA_CREACION", "TITULO" y "PRIORIDAD"
        return Arrays.stream(Orden.values())
                .map(orden -> OpcionCatalogo.de(orden.name(), orden.getEtiqueta()))
                .toList();
    }
}
