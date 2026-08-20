# Levantamiento de requerimientos — Mini Help Desk


## 1. Problema

En la organización los problemas técnicos se avisan por mensajería informal:
un mensaje al grupo, un correo suelto, una llamada al pasillo. Como no existe
un lugar único donde queden registrados, ocurren tres cosas de forma sistemática.
Primero, se pierde información: nadie recuerda quién reportó qué ni desde cuándo
está pendiente. Segundo, no hay forma de saber cuántos casos están sin atender ni
cuáles son urgentes, así que la priorización termina dependiendo de quién insiste
más. Tercero, cuando un caso se resuelve no queda rastro de lo que se hizo, y el
mismo problema se vuelve a diagnosticar desde cero la próxima vez.

Quien sufre el problema es el equipo de soporte interno, que atiende los casos, y
de rebote las personas de la organización que reportan y no reciben respuesta
oportuna. Hoy, sin una herramienta, el costo se paga en casos olvidados, tiempo
gastado en reconstruir qué estaba pendiente y falta de visibilidad para
justificar carga de trabajo o comprar equipamiento.

La aplicación debe producir un registro único y consultable de las incidencias
técnicas: cada caso queda anotado con quién lo reporta, qué ocurre, cuán urgente
es y en qué situación está; el operador puede encontrar rápidamente los casos que
le interesan y actualizar su avance; y en todo momento existe una cifra confiable
de cuántas incidencias están abiertas, en atención y resueltas.

---

## 2. Actores

Para el alcance de este diagnóstico existe **un solo actor principal**. No hay
autenticación ni perfiles diferenciados: cualquier persona que abre la aplicación
opera como operador de soporte.

### Operador de soporte

| Aspecto | Descripción |
|---|---|
| **Qué necesita hacer** | Registrar una incidencia que le reportaron; revisar la lista de casos pendientes; encontrar un caso puntual; actualizar los datos si le llega más información; marcar el avance (tomado / resuelto); dar de baja registros erróneos o duplicados. |
| **Qué información utiliza** | Título y descripción del problema, quién lo reporta, categoría, prioridad, estado actual, fecha en que se registró y el historial de cambios de estado. |
| **Qué resultado espera** | Saber, en menos de un vistazo, cuántos casos están pendientes y cuáles son urgentes; poder demostrar qué se hizo con un caso y cuándo. |

**Actor secundario (indirecto):** la persona que reporta el problema. No usa la aplicación en esta versión, le informa al operador por los canales actuales pero su nombre queda registrado en el campo *solicitante*, para saber a quién volver a contactar.

---

## 3. Requerimientos funcionales

### RF-01 — Registrar incidencia
El sistema debe permitir registrar una incidencia indicando título, descripción,
categoría, prioridad y solicitante. El estado inicial **no** es un dato que el
usuario informe: lo asigna el sistema.

### RF-02 — Listar incidencias
El sistema debe mostrar el listado de incidencias registradas con su título,
descripción resumida, categoría, prioridad, estado, solicitante y antigüedad.

### RF-03 — Consultar el detalle de una incidencia
El sistema debe permitir abrir una incidencia y ver todos sus datos completos,
incluidas las fechas de creación, última actualización y resolución.

### RF-04 — Editar una incidencia
El sistema debe permitir modificar el título, la descripción, la categoría, la
prioridad y el solicitante de una incidencia ya registrada.

### RF-05 — Eliminar una incidencia
El sistema debe permitir eliminar una incidencia, previa confirmación explícita
del usuario, junto con su historial asociado.

### RF-06 — Cambiar el estado de una incidencia
El sistema debe permitir avanzar una incidencia entre los estados definidos
mediante una acción directa desde el listado, sin necesidad de abrir el
formulario de edición completo.

### RF-07 — Buscar incidencias por texto
El sistema debe permitir buscar incidencias por una palabra o frase que aparezca
en el título, en la descripción o en el nombre del solicitante, sin distinguir
mayúsculas de minúsculas.

### RF-08 — Filtrar incidencias
El sistema debe permitir filtrar el listado por estado, por prioridad y por
categoría. Los filtros deben poder combinarse entre sí y con la búsqueda por
texto.

### RF-09 — Ordenar el listado
El sistema debe permitir ordenar las incidencias por fecha de registro
(descendente o ascendente), por prioridad de mayor a menor y por título.

### RF-10 — Consultar el resumen por estado
El sistema debe mostrar de forma permanente cuántas incidencias hay en cada
estado y cuántas hay en total. Estas cifras deben reflejar el total registrado,
no solo lo que dejó visible el filtro activo.

### RF-11 — Consultar el historial de cambios de estado
El sistema debe registrar automáticamente cada transición de estado de una
incidencia, con su estado de origen, estado de destino, comentario y fecha, y
permitir consultarla desde el detalle.

### RF-12 — Obtener los catálogos de valores válidos
El sistema debe exponer los valores aceptados de estado, prioridad, categoría y
criterio de ordenamiento, de modo que la interfaz construya sus selectores a
partir de esa información y no de una copia escrita a mano.

---

## 4. Requerimientos no funcionales

### RNF-01 — Ejecución reproducible
La solución completa (frontend, backend y base de datos) debe iniciarse con un
único comando, `docker compose up --build`, siguiendo el README, en una máquina
que solo tenga Docker instalado.
**Criterio observable:** una persona ajena al desarrollo clona el repositorio,
ejecuta el comando y accede a la aplicación en `http://localhost:5173` sin
instalar Java, Node ni PostgreSQL.

### RNF-02 — Persistencia de los datos
Los datos deben sobrevivir al ciclo de vida de los contenedores.
**Criterio observable:** tras `docker compose down` seguido de
`docker compose up -d`, las incidencias creadas antes siguen listadas. Solo
`docker compose down -v` las elimina, porque destruye el volumen.

### RNF-03 — Manejo de errores comprensible
Toda respuesta de error de la API debe tener el mismo formato JSON, con código
HTTP coherente y un mensaje redactado en español entendible para una persona.
**Criterio observable:** enviar un título de 2 caracteres devuelve HTTP 400 con
el mensaje del campo específico; pedir `/api/incidencias/9999` devuelve HTTP 404;
resolver dos veces la misma incidencia devuelve HTTP 409. En los tres casos el
cuerpo tiene las mismas claves.

### RNF-04 — Usabilidad
La interfaz debe permitir completar cualquier operación del CRUD sin
instrucciones previas, informar el resultado de cada acción y pedir confirmación
antes de una operación destructiva.
**Criterio observable:** eliminar exige confirmación; cada creación, edición,
cambio de estado o eliminación muestra un aviso de éxito o de error; los campos
obligatorios muestran su error bajo el input correspondiente.

### RNF-05 — Accesibilidad básica
La aplicación debe ser operable con teclado y comprensible sin depender del
color.
**Criterio observable:** todos los controles son alcanzables con Tab y muestran
un indicador de foco visible; la prioridad se comunica con barras además de
color; los avisos se anuncian mediante `aria-live`.

### RNF-06 — Mantenibilidad
El código debe estar organizado por responsabilidad, de modo que un integrante
pueda ubicar dónde se agrega un endpoint, dónde se valida un dato y dónde se
consulta la base de datos sin recorrer todo el proyecto.
**Criterio observable:** el backend separa `controller`, `service`,
`models/entities`, `repository`, `dto`, `mapper` y `exception`; el frontend
separa el cliente HTTP de los componentes de presentación.

### RNF-07 — Configuración sin recompilar
Host, puerto, nombre de base de datos, credenciales y URL de la API deben poder
cambiarse mediante variables de entorno.
**Criterio observable:** ningún valor de conexión aparece escrito directamente en
el código Java ni en el JavaScript; Compose inyecta la configuración con
variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
`SPRING_DATASOURCE_PASSWORD`, `POSTGRES_*`, `FRONTEND_PORT`) y `.env.example` las
documenta. La URL de la API es la ruta relativa `/api`, resuelta por Nginx, así
que no depende de ninguna variable de build.

### RNF-08 — No versionar secretos
El repositorio no debe contener contraseñas ni credenciales personales.
**Criterio observable:** `.env` está en `.gitignore`; el repositorio solo
incluye `.env.example` con credenciales de desarrollo declaradas como tales.

---

## 5. Reglas de negocio

Todas las reglas de esta sección están implementadas, no solo documentadas. La
columna "dónde vive" indica el archivo responsable.

### RN-01 — Estado inicial
Toda incidencia nueva comienza en estado **ABIERTA**. El cliente no puede enviar
un estado distinto al crearla, porque el DTO de creación no tiene ese campo.
*Dónde vive:* `Incidencia.registrar()` y `CrearIncidenciaRequest`.

### RN-02 — Transiciones de estado permitidas
Los cambios de estado válidos son:

```
ABIERTA      ──▶ EN_PROGRESO,  RESUELTA
EN_PROGRESO  ──▶ ABIERTA,      RESUELTA
RESUELTA     ──▶ (ninguna: es un estado terminal)
```

Cualquier otra transición, incluido repetir el estado actual, se rechaza con
HTTP 409.
*Dónde vive:* `Estado.permiteTransicionA()` y `Incidencia.cambiarEstado()`.

### RN-03 — Fecha de resolución
Una incidencia tiene fecha de resolución si y solo si su estado es RESUELTA. La
fecha la asigna el sistema al momento de la transición; no se pide al usuario.
*Dónde vive:* `Incidencia.cambiarEstado()`. No hay una restricción `CHECK` a
nivel de base de datos (ver DA-07); la coherencia se sostiene en el código, que
es la única puerta de entrada a los datos.

### RN-04 — Trazabilidad de los cambios de estado
Cada transición de estado —incluido el registro inicial— genera automáticamente
una entrada en el historial con estado de origen, estado de destino, comentario
opcional y fecha. El historial no se puede escribir por separado.
*Dónde vive:* `Incidencia.agregarAlHistorial()`, invocado únicamente desde
`registrar()` y `cambiarEstado()`.

### RN-05 — Inmutabilidad de las incidencias resueltas
Una incidencia en estado terminal no admite modificación de sus datos ni nuevos
cambios de estado. La interfaz deshabilita el botón "Editar" y el backend
devuelve HTTP 409 si la petición llega igual.
*Dónde vive:* `Incidencia.actualizarDatos()`.

### RN-06 — Identificadores inexistentes
Una operación sobre un identificador que no existe devuelve HTTP 404 con un
mensaje explícito. Nunca se responde 200 con un cuerpo vacío ni se crea el
recurso de forma implícita.
*Dónde vive:* `IncidenciaService.buscarOFallar()` y
`ManejadorGlobalErrores.noEncontrado()`.

### RN-07 — Datos obligatorios y valores de catálogo
Título (5–120 caracteres), descripción (10–2000), categoría, prioridad y
solicitante (3–80) son obligatorios. Categoría, prioridad y estado solo aceptan
valores del catálogo. La validación se aplica en el backend; el frontend hace una
comprobación previa por comodidad, pero no es la barrera.
*Dónde vive:* anotaciones de Bean Validation en los DTO, enums tipados de Java
(`Estado`, `Prioridad`, `Categoria`) y `ManejadorGlobalErrores`. La base de
datos no agrega restricciones `CHECK` porque el esquema lo crea Hibernate
(ver DA-07).

---

## 6. Criterios de aceptación

### CA-01 — Registro de una incidencia (RF-01, RN-01, RN-04)

```gherkin
Dado que el operador completó título, descripción, categoría,
      prioridad y solicitante con datos válidos
Cuando presiona "Registrar incidencia"
Entonces la incidencia queda almacenada en la base de datos
Y aparece en el listado principal
Y su estado inicial es ABIERTA
Y su historial contiene una entrada de registro
Y el contador de incidencias abiertas aumenta en uno
```

### CA-02 — Rechazo de datos inválidos (RNF-03, RN-07)

```gherkin
Dado que el operador escribió un título de 2 caracteres
Cuando intenta registrar la incidencia
Entonces el sistema no la almacena
Y la API responde con código HTTP 400
Y el formulario muestra, bajo el campo Título,
  el mensaje "El título debe tener entre 5 y 120 caracteres."
```

### CA-03 — Transición de estado no permitida (RF-06, RN-02, RN-05)

```gherkin
Dada una incidencia en estado RESUELTA
Cuando se envía un cambio de estado a EN_PROGRESO
Entonces el sistema mantiene la incidencia en RESUELTA
Y la API responde con código HTTP 409
Y el mensaje indica que la transición no está permitida
Y no se agrega ninguna entrada al historial
```

### CA-04 — Filtro combinado con búsqueda (RF-07, RF-08, RF-10)

```gherkin
Dado que existen incidencias en distintos estados y prioridades
Cuando el operador selecciona el estado ABIERTA
Y escribe "impresora" en el campo de búsqueda
Entonces el listado muestra solo las incidencias abiertas
  cuyo título, descripción o solicitante contiene "impresora"
Y el filtrado lo resuelve el backend mediante parámetros de consulta
Y el contador por estado sigue mostrando el total registrado,
  no solo las incidencias visibles en pantalla
```

---

## 7. Alcance

### Sí está dentro del alcance

- CRUD completo de incidencias, integrado de extremo a extremo.
- Búsqueda por texto, filtro por estado, filtro por prioridad, filtro por
  categoría, ordenamiento, cambio rápido de estado, contador por estado e
  historial de cambios (seis de las capacidades adicionales sugeridas; el mínimo
  exigido era dos).
- Validación en el backend y manejo uniforme de errores.
- Persistencia en PostgreSQL con esquema gestionado por Hibernate.
- Ejecución completa con Docker Compose y datos persistentes en un volumen.
- Documentación de requerimientos, arquitectura y decisiones técnicas.

### No está dentro del alcance

Estas exclusiones son deliberadas: cada una agregaría trabajo que no aporta
evidencia adicional sobre las competencias que el diagnóstico busca observar.

| Fuera de alcance | Por qué |
|---|---|
| Autenticación, login y perfiles de usuario | Hay un solo actor; agregar Spring Security desviaría el foco de la integración fullstack. |
| Recuperación de contraseña, envío de correo, notificaciones push | Dependen de servicios externos que no se pueden levantar de forma reproducible con `docker compose up`. |
| Asignación de la incidencia a un técnico responsable | Requiere modelar usuarios, que quedaron fuera. |
| Adjuntar archivos o capturas de pantalla | Implica almacenamiento de binarios, fuera del alcance del diagnóstico. |
| Paginación del listado | Con el volumen esperado el listado completo se resuelve en una consulta. Es la primera deuda a pagar si el volumen crece. |
| Microservicios, mensajería, caché distribuida | La actividad pide explícitamente evitar arquitectura sobredimensionada. |
| Despliegue en la nube y pipeline de CI/CD | Corresponde a las unidades siguientes de la asignatura. |
| Suite completa de pruebas automatizadas | Solo existe la prueba de arranque del contexto (`BackendApplicationTests`); aún no hay pruebas unitarias de las reglas de dominio, ni de integración ni end-to-end. Escribirlas es viable y directo, pero no se exigía. |
