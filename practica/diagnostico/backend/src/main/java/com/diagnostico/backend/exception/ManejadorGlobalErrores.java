package com.diagnostico.backend.exception;

import com.diagnostico.backend.dto.DetalleCampo;
import com.diagnostico.backend.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@RestControllerAdvice // RestControllerAdvice indica que esta clase es un manejador global de excepciones, y que los metodos de esta clase van a manejar las excepciones lanzadas por los controladores de Spring Boot
public class ManejadorGlobalErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException ex, HttpServletRequest peticion) { // Metodo que maneja las excepciones lanzadas cuando los datos de la peticion no son validos, por ejemplo cuando un campo es nulo o tiene un valor no aceptado
        List<DetalleCampo> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new DetalleCampo(error.getField(), error.getDefaultMessage()))
                .toList();

        String resumen = detalles.stream()
                .map(DetalleCampo::mensaje)
                .collect(Collectors.joining(" "));

        return construir(HttpStatus.BAD_REQUEST, "Datos inválidos", resumen.isBlank() ? "La petición contiene datos inválidos." : resumen, peticion, detalles);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> tipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest peticion) { // Metodo que maneja las excepciones lanzadas cuando un parametro de la peticion tiene un tipo de dato no valido, por ejemplo cuando se espera un entero y se recibe un string
        String mensaje = "El parámetro '%s' recibió el valor '%s', que no es válido."
                .formatted(ex.getName(), ex.getValue());

        Class<?> tipoEsperado = ex.getRequiredType();
        if (tipoEsperado != null && tipoEsperado.isEnum()) {
            String aceptados = Arrays.stream(tipoEsperado.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            mensaje += " Valores aceptados: " + aceptados + ".";
        }

        return construir(HttpStatus.BAD_REQUEST, "Parámetro inválido", mensaje, peticion, List.of(new DetalleCampo(ex.getName(), mensaje)));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoIlegible(HttpMessageNotReadableException ex, HttpServletRequest peticion) { // Metodo que maneja las excepciones lanzadas cuando el cuerpo de la peticion no es un JSON valido o contiene un valor no aceptado en un campo de catalogo (estado, prioridad o categoria)
        log.debug("Cuerpo de petición ilegible en {}: {}", peticion.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.BAD_REQUEST, "Cuerpo inválido", "El cuerpo de la petición no es un JSON válido o contiene un valor no aceptado " + "en un campo de catálogo (estado, prioridad o categoría).", peticion, List.of());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> noEncontrado(RecursoNoEncontradoException ex, HttpServletRequest peticion) { // Metodo que maneja las excepciones lanzadas cuando no se encuentra un recurso solicitado, por ejemplo cuando se busca una incidencia por su id y no existe
        return construir(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), peticion, List.of());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> reglaNegocio(ReglaNegocioException ex, HttpServletRequest peticion) { // Metodo que maneja las excepciones lanzadas cuando se incumple una regla de negocio, por ejemplo cuando se intenta cambiar el estado de una incidencia a un estado no permitido
        return construir(HttpStatus.CONFLICT, "Regla de negocio", ex.getMessage(), peticion, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> noControlada(Exception ex, HttpServletRequest peticion) { // Metodo que maneja las excepciones no controladas, es decir, aquellas que no son capturadas por los otros metodos, por ejemplo cuando ocurre un error inesperado en el backend
        log.error("Error no controlado en {} {}", peticion.getMethod(), peticion.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado. Revise los logs del backend.", peticion, List.of());
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus estado, String error, String mensaje, HttpServletRequest peticion, List<DetalleCampo> detalles) { // Metodo que construye la respuesta de error, con el estado HTTP, el mensaje de error, el mensaje detallado, la URI de la peticion y los detalles de los campos con errores
        ErrorResponse cuerpo = ErrorResponse.de(estado.value(), error, mensaje, peticion.getRequestURI(), detalles);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
