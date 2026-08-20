# Mini Help Desk

Registro y seguimiento de incidencias técnicas.

## Integrantes

- Juan Carlos Tapia
- Fernando Camus
- Franco Garay
- Deymon Gonzalez

## Problema que resuelve

En la organización las incidencias técnicas se reportan por canales informales
(mensajería, correo, llamadas), sin un lugar único donde queden registradas.
Eso produce tres fallas sistemáticas: se pierde información (quién reportó,
desde cuándo está pendiente), no hay visibilidad para priorizar (cuántos casos
hay sin atender y cuáles son urgentes) y no queda rastro de lo que se hizo al
resolver (el mismo problema se diagnostica desde cero cada vez).

La aplicación crea un **registro único y consultable** de incidencias: cada caso
queda anotado con su solicitante, descripción, categoría, prioridad y estado, con
historial de cambios y contadores por estado en todo momento.

## Tecnologías utilizadas

| Capa | Tecnología |
|---|---|
| Frontend | React 19 + Vite, servido por Nginx |
| Backend | Java 21 + Spring Boot 4 (Spring Data JPA, Hibernate, Bean Validation) |
| Base de datos | PostgreSQL 16 |
| Infraestructura | Docker Compose (3 contenedores) |

## Arquitectura resumida

Tres servicios Docker comunicados por la red interna de Compose:

```text
Navegador ── :5173 ──▶ frontend (Nginx :80)
                          │  proxy /api
                          ▼
                      backend (Tomcat :8080)
                          │  JDBC
                          ▼
                      database (PostgreSQL :5432)
                          │
                          ▼
                  volumen postgres_data
```

- Nginx sirve los archivos estáticos y reenvía todo lo que empiece con `/api`
  al backend. El navegador solo conoce `http://localhost:5173`.
- El backend es el único que habla con la base de datos; la API expone el CRUD
  completo más catálogos, resumen e historial.
- Los datos viven en el volumen `postgres_data` y sobreviven a `docker compose
  down`; solo `docker compose down -v` los elimina.

Detalle completo en [`docs/arquitectura.md`](docs/arquitectura.md).

## Requisitos para ejecutar

- Docker y Docker Compose instalados. No se instala Java, Node ni PostgreSQL:
  las imágenes se construyen a partir de los `Dockerfile` de `backend/` y
  `frontend/`.
- La primera compilación descarga las dependencias de Maven y npm y puede tardar
  varios minutos; las siguientes son mucho más rápidas por el cacheo de capas.

## Variables de entorno necesarias

Los valores de conexión, credenciales y el puerto del frontend se definen en un
archivo `.env` en la raíz. Copie `.env.example` como `.env` y ajuste lo que
necesite:

```bash
cp .env.example .env
```

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `POSTGRES_DB` | Nombre de la base de datos | `helpdesk` |
| `POSTGRES_USER` | Usuario de la base de datos | `helpdesk` |
| `POSTGRES_PASSWORD` | Contraseña de la base de datos | `helpdesk` |
| `FRONTEND_PORT` | Puerto del frontend publicado en el host | `5173` |
| `VITE_API_BASE_URL` | URL base de la API (desarrollo con Vite) | `/api` |

Las credenciales de `.env.example` son solo de desarrollo. El backend recibe la
configuración vía `SPRING_DATASOURCE_*` inyectadas por Compose; la URL de la API
es la ruta relativa `/api`, resuelta por Nginx, por lo que no requiere ninguna
variable de build.

## Comando para levantar la solución

```bash
docker compose up --build
```

## URLs y puertos esperados

| Servicio | Puerto en el host | Puerto interno | URL |
|---|---|---|---|
| Frontend (Nginx) | `5173` (o `FRONTEND_PORT`) | `80` | <http://localhost:5173> |
| API | — (a través del proxy) | `8080` (backend) | <http://localhost:5173/api> |
| Base de datos | — (sin publicar) | `5432` | — |

## Cómo verificar rápidamente el CRUD

Con los contenedores levantados, la API se alcanza en `http://localhost:5173/api`.

```bash
# 1. Crear (CREATE) → 201 Created, estado ABIERTA
curl -i -X POST http://localhost:5173/api/incidencias \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Teclado del laboratorio no responde","descripcion":"Algunas teclas no registran.","categoria":"HARDWARE","prioridad":"BAJA","solicitante":"Juan Tapia"}'

# 2. Listar (READ) → 200, la incidencia aparece en el listado
curl -s http://localhost:5173/api/incidencias

# 3. Ver detalle (READ por id) → 200 con historial
curl -s http://localhost:5173/api/incidencias/<id>

# 4. Editar (UPDATE) → 200
curl -i -X PUT http://localhost:5173/api/incidencias/<id> \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Teclado del laboratorio no responde (equipo 4)","descripcion":"Las teclas A, S y D no registran.","categoria":"HARDWARE","prioridad":"MEDIA","solicitante":"Juan Tapia"}'

# 5. Cambiar estado → 200, se agrega entrada al historial
curl -i -X PATCH http://localhost:5173/api/incidencias/<id>/estado \
  -H "Content-Type: application/json" \
  -d '{"nuevoEstado":"EN_PROGRESO","comentario":"Se solicitó teclado de reemplazo"}'

# 6. Eliminar (DELETE) → 204; repetirlo devuelve 404
curl -i -X DELETE http://localhost:5173/api/incidencias/<id>
```

Guía paso a paso completa (incluida la verificación desde la interfaz y la
comprobación de persistencia): [`docs/verificacion.md`](docs/verificacion.md).

## Funcionalidades adicionales implementadas

- **Búsqueda por texto** insensible a mayúsculas sobre título, descripción y
  solicitante (`?buscar=...`).
- **Filtros combinables** por estado, prioridad y categoría, combinables con la
  búsqueda.
- **Ordenamiento** por fecha (asc/desc), prioridad (de mayor a menor) y título.
- **Cambio rápido de estado** desde el listado, validado por el backend
  (transiciones permitidas en `ABIERTA → EN_PROGRESO/RESUELTA`).
- **Resumen por estado**: contadores permanentes de abiertas, en progreso,
  resueltas y total.
- **Historial de cambios de estado** automático, consultable desde el detalle.
- **Catálogos dinámicos** (`/api/catalogos`): la interfaz construye sus
  selectores a partir de la API, no de una copia escrita a mano.
- **Validación en el backend** y formato único de error JSON (400/404/409/500)
  con detalle por campo.
- **Regla de estado terminal**: una incidencia resuelta no admite edición ni
  nuevas transiciones (HTTP 409).

## Limitaciones conocidas

| Limitación | Impacto |
|---|---|
| El esquema lo crea Hibernate con `ddl-auto: update`, sin migraciones versionadas ni restricciones `CHECK` | No hay historial de cambios de esquema; las reglas viven solo en el código de la entidad |
| El listado no está paginado | Con miles de incidencias la respuesta crece sin control |
| `resumen` ejecuta una consulta `count` por estado | Tres consultas donde bastaría una con `GROUP BY` |
| El backend no publica puerto en el host y no tiene healthcheck | Solo se opera vía el proxy de Nginx; Compose no detecta un arranque fallido |
| Sin autenticación | Cualquiera con acceso a la red puede operar la API (fuera de alcance declarado) |
| Búsqueda con `LIKE %texto%` | No usa índice; degrada con muchas filas |
| Sin suite completa de pruebas automatizadas | Solo existe la prueba de arranque del contexto (`BackendApplicationTests`) |
| Las credenciales viajan como variables de entorno en claro | Aceptable en desarrollo local, no en producción |

## Documentación

- [`docs/requerimientos.md`](docs/requerimientos.md) — levantamiento de requerimientos, reglas de negocio y criterios de aceptación.
- [`docs/arquitectura.md`](docs/arquitectura.md) — arquitectura, modelo de datos y contrato de la API.
- [`docs/decisiones.md`](docs/decisiones.md) — decisiones técnicas y sus alternativas.
- [`docs/verificacion.md`](docs/verificacion.md) — guía paso a paso para comprobar la solución.
