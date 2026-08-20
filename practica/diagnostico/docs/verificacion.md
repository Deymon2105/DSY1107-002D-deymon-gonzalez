# Guía de verificación

Comandos concretos para comprobar que la solución cumple lo exigido. Todo se
ejecuta con los contenedores levantados (`docker compose up --build`).

Los ejemplos usan `curl` contra `http://localhost:5173/api/...`. El backend no
publica puerto en el host, así que la API se alcanza a través del proxy de Nginx
del frontend (que reenvía `/api` al backend). Si cambió `FRONTEND_PORT` en
`.env`, reemplace `5173` por el puerto configurado.

> En Windows con PowerShell, use `curl.exe` en lugar de `curl` (el alias de
> PowerShell apunta a otro comando) y reemplace `\` por comillas simples al
> partir líneas.

---

## 1. Los servicios están arriba

```bash
docker compose ps
```

Los tres servicios (`frontend`, `backend`, `database`) deben aparecer como
`running`, y `database` además como `healthy`. El backend no tiene healthcheck;
su estado se comprueba a través de la API:

```bash
curl -s http://localhost:5173/api/catalogos
```

Debe devolver JSON con los catálogos de estados, prioridades, categorías y
ordenamientos. Si responde, el backend está operativo y el proxy funciona.

---

## 2. CRUD completo por API

La base arranca **vacía**: no hay datos de demostración. Los pasos siguientes
crean sus propios datos; en el paso 2.2 anote el `id` devuelto para usarlo en
los pasos restantes.

### 2.1 Listar (READ)

```bash
curl -s http://localhost:5173/api/incidencias
```

Debe devolver `[]` la primera vez (no hay incidencias registradas). Después de
crear una, aparece en el listado.

### 2.2 Crear (CREATE)

```bash
curl -i -X POST http://localhost:5173/api/incidencias \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Teclado del laboratorio no responde",
    "descripcion": "Algunas teclas no registran al escribir en el equipo 4 del laboratorio.",
    "categoria": "HARDWARE",
    "prioridad": "BAJA",
    "solicitante": "Juan Tapia"
  }'
```

**Esperado:** `201 Created`, cabecera `Location: /api/incidencias/<id>` y en el
cuerpo `"estado": "ABIERTA"` con una entrada en `historial` (RN-01, RN-04).
Reemplace `<id>` por ese valor en los pasos siguientes.

### 2.3 Ver detalle (READ por id)

```bash
curl -s http://localhost:5173/api/incidencias/<id>
```

**Esperado:** `200` con la incidencia y su historial completo.

### 2.4 Editar (UPDATE)

```bash
curl -i -X PUT http://localhost:5173/api/incidencias/<id> \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Teclado del laboratorio no responde (equipo 4)",
    "descripcion": "Las teclas A, S y D no registran. Se probó con otro cable USB sin resultado.",
    "categoria": "HARDWARE",
    "prioridad": "MEDIA",
    "solicitante": "Juan Tapia"
  }'
```

**Esperado:** `200` con los datos actualizados y `fechaActualizacion` distinta de
`fechaCreacion`.

### 2.5 Cambiar de estado

```bash
curl -i -X PATCH http://localhost:5173/api/incidencias/<id>/estado \
  -H "Content-Type: application/json" \
  -d '{"nuevoEstado": "EN_PROGRESO", "comentario": "Se solicitó teclado de reemplazo"}'
```

**Esperado:** `200`, `"estado": "EN_PROGRESO"` y una segunda entrada en el
historial.

### 2.6 Eliminar (DELETE)

```bash
curl -i -X DELETE http://localhost:5173/api/incidencias/<id>
```

**Esperado:** `204 No Content` sin cuerpo. Repetir el mismo comando devuelve
`404`.

---

## 3. Funcionalidades adicionales

Los resultados de esta sección dependen de las incidencias que existan en ese
momento; cree o conserve algunas (sección 2) para que los ejemplos devuelvan
datos.

### Búsqueda por texto

```bash
curl -s "http://localhost:5173/api/incidencias?buscar=teclado"
```

Debe devolver solo las incidencias cuyo título, descripción o solicitante
contiene "teclado". La búsqueda es insensible a mayúsculas:
`buscar=TECLADO` da el mismo resultado.

### Filtro por estado

```bash
curl -s "http://localhost:5173/api/incidencias?estado=ABIERTA"
```

### Filtro por prioridad

```bash
curl -s "http://localhost:5173/api/incidencias?prioridad=ALTA"
```

### Filtros combinados con ordenamiento

```bash
curl -s "http://localhost:5173/api/incidencias?estado=ABIERTA&orden=PRIORIDAD"
```

Devuelve solo las abiertas, ordenadas de ALTA a BAJA. Es la comprobación de que
el orden por prioridad usa el nivel numérico y no el texto (que daría ALTA,
BAJA, MEDIA).

### Contador por estado

```bash
curl -s http://localhost:5173/api/incidencias/resumen
```

Devuelve `{"total":...,"abiertas":...,"enProgreso":...,"resueltas":...}` con los
totales registrados. Los números dependen de lo que se haya creado; la suma de
los tres primeros debe coincidir con `total`.

### Catálogos

```bash
curl -s http://localhost:5173/api/catalogos
```

---

## 4. Reglas de negocio y manejo de errores

### Título demasiado corto → 400 con detalle por campo

```bash
curl -i -X POST http://localhost:5173/api/incidencias \
  -H "Content-Type: application/json" \
  -d '{"titulo":"ab","descripcion":"x","categoria":"RED","prioridad":"BAJA","solicitante":"y"}'
```

**Esperado:** `400`, con `detalles` indicando los campos `titulo`, `descripcion`
y `solicitante`.

### Identificador inexistente → 404 (RN-06)

```bash
curl -i http://localhost:5173/api/incidencias/999999
```

**Esperado:** `404` con `"mensaje": "No existe una incidencia con id 999999."`

### Transición no permitida → 409 (RN-02)

Cree una incidencia cualquiera (sección 2.2) y reemplace `<id>`. Primero llévela
a RESUELTA en dos transiciones válidas y luego intente reabrirla:

```bash
curl -i -X PATCH http://localhost:5173/api/incidencias/<id>/estado \
  -H "Content-Type: application/json" \
  -d '{"nuevoEstado": "EN_PROGRESO"}'

curl -i -X PATCH http://localhost:5173/api/incidencias/<id>/estado \
  -H "Content-Type: application/json" \
  -d '{"nuevoEstado": "RESUELTA"}'

curl -i -X PATCH http://localhost:5173/api/incidencias/<id>/estado \
  -H "Content-Type: application/json" \
  -d '{"nuevoEstado": "EN_PROGRESO"}'
```

**Esperado:** las dos primeras responden `200`; la tercera, que intenta reabrir
una incidencia en estado terminal, responde `409` explicando que la transición
no está permitida.

### Editar una incidencia resuelta → 409 (RN-05)

Sobre la misma incidencia RESUELTA del paso anterior:

```bash
curl -i -X PUT http://localhost:5173/api/incidencias/<id> \
  -H "Content-Type: application/json" \
  -d '{
    "titulo":"Intento de edición sobre incidencia cerrada",
    "descripcion":"Esta edición debe ser rechazada por el backend.",
    "categoria":"HARDWARE","prioridad":"ALTA","solicitante":"Prueba"
  }'
```

**Esperado:** `409`.

### Valor fuera del catálogo → 400 con los valores aceptados

```bash
curl -i "http://localhost:5173/api/incidencias?estado=CERRADA"
```

**Esperado:** `400` con el mensaje indicando que los valores aceptados son
`ABIERTA, EN_PROGRESO, RESUELTA`.

---

## 5. Persistencia (RNF-02)

Esta es la comprobación que exige el desafío. **No** use `-v` en el `down`.

```bash
# 1. Levantar y crear un dato reconocible
docker compose up -d
curl -s -X POST http://localhost:5173/api/incidencias \
  -H "Content-Type: application/json" \
  -d '{
    "titulo":"PRUEBA DE PERSISTENCIA",
    "descripcion":"Esta incidencia debe seguir existiendo después de reiniciar los contenedores.",
    "categoria":"OTRO","prioridad":"BAJA","solicitante":"Revisor"
  }'

# 2. Detener y eliminar los contenedores (el volumen NO se toca)
docker compose down

# 3. Volver a levantar
docker compose up -d

# 4. Verificar que el dato sigue ahí
curl -s "http://localhost:5173/api/incidencias?buscar=PERSISTENCIA"
```

**Esperado:** el paso 4 devuelve la incidencia creada en el paso 1.

Para confirmar el mecanismo, el volumen debe existir:

```bash
docker volume ls | grep postgres_data
# local     postgres_data
```

Y si se elimina explícitamente el volumen, los datos sí desaparecen:

```bash
docker compose down -v      # esto SÍ borra el volumen y los datos
docker compose up -d        # la base parte vacía y Hibernate vuelve a crear el esquema
```

---

## 6. Inspección directa de la base de datos

```bash
docker compose exec database psql -U helpdesk -d helpdesk -c "\dt"
```

```text
             List of relations
 Schema |      Name      | Type  |  Owner
--------+----------------+-------+----------
 public | cambio_estado  | table | helpdesk
 public | incidencia     | table | helpdesk
```

```bash
# Conteo por estado directo en SQL
docker compose exec database psql -U helpdesk -d helpdesk \
  -c "SELECT estado, COUNT(*) FROM incidencia GROUP BY estado ORDER BY estado;"

# Historial de una incidencia
docker compose exec database psql -U helpdesk -d helpdesk \
  -c "SELECT incidencia_id, estado_anterior, estado_nuevo, fecha FROM cambio_estado WHERE incidencia_id = <id> ORDER BY fecha;"
```

---

## 7. Verificación desde la interfaz

1. Abrir `http://localhost:5173`.
2. Comprobar que las cuatro tarjetas de resumen muestran cifras.
3. **Crear:** botón "Nueva incidencia", completar y registrar. Aparece el aviso
   de éxito, la incidencia se agrega al listado y el contador de abiertas sube.
4. **Validar:** volver a abrir el formulario, escribir un título de 2 letras e
   intentar registrar. El error aparece bajo el campo.
5. **Leer:** clic en el título de una incidencia. Se abre el detalle con el
   historial.
6. **Editar:** botón "Editar", cambiar la prioridad, guardar. El medidor de
   prioridad cambia en el listado.
7. **Cambiar estado:** botón "Tomar" y luego "Resolver". La franja lateral y la
   píldora cambian de color, y el resumen se recalcula.
8. **Regla de negocio:** en la incidencia que acaba de resolver, el botón
   "Editar" queda deshabilitado y no quedan botones de transición.
9. **Buscar y filtrar:** escribir "teclado" en la búsqueda; el listado se
   reduce y el contador de arriba **no** cambia, porque muestra el total
   registrado y no lo visible.
10. **Ordenar:** cambiar a "Mayor prioridad primero" y verificar que las de
    prioridad alta quedan arriba.
11. **Eliminar:** botón "Eliminar"; aparece la confirmación y, al aceptar, la
    incidencia desaparece del listado.
12. **Teclado:** recorrer la pantalla con Tab y comprobar que el foco es visible
    en cada control y que Escape cierra las ventanas modales.

---

## 8. Pruebas automatizadas del backend

Requiere Maven y JDK 21 instalados localmente. No requiere Docker ni base de
datos.

```bash
cd backend
mvn test
```

**Esperado:** 1 prueba ejecutada (la carga del contexto de Spring Boot),
ninguna fallida. Aún no hay pruebas unitarias de las reglas de dominio; la única
prueba verifica que la aplicación arranca. El `Dockerfile` omite las pruebas al
construir (`-DskipTests`), así que esta verificación se hace en el entorno de
desarrollo.

---

## 9. Diagnóstico de problemas frecuentes

| Síntoma | Causa probable | Qué hacer |
|---|---|---|
| `bind: address already in use` | Otro proceso ocupa 5173 (o el puerto configurado) | Cambiar `FRONTEND_PORT` en `.env` y volver a levantar |
| El frontend carga pero el listado queda vacío con un error | El backend aún no termina de arrancar | Esperar unos segundos y presionar "Reintentar"; revisar con `docker compose logs -f backend` |
| `host "database" not found` en el log del backend | Se levantó el backend sin la red de Compose | Usar siempre `docker compose up`, nunca `docker run` suelto |
| Error de mapeo de esquema al arrancar o en una consulta | Una entidad cambió y la base conserva columnas antiguas | Durante el desarrollo, `docker compose down -v` para partir de cero (no hay migraciones versionadas) |
| Los cambios en el código no se reflejan | La imagen quedó cacheada | `docker compose up --build`, o `docker compose build --no-cache <servicio>` |
| El frontend responde 502 al llamar `/api` | El backend está caído o aún arrancando | `docker compose logs backend` y revisar la conexión a la base |
