# Decisiones técnicas — Mini Help Desk

Cada decisión sigue el mismo formato: **contexto**, **alternativas
consideradas**, **decisión**, **justificación** y **consecuencias**, incluyendo
las desventajas que se aceptaron a sabiendas.

El stack general —Java 21, Spring Boot 4, React, base de datos relacional,
Docker— es una restricción de la actividad y no se justifica aquí. Lo que sigue
son las decisiones tomadas *dentro* de ese stack.

---

## DA-01 — React + Vite en lugar de Next.js

**Contexto.**
La aplicación tiene una sola pantalla: un listado con filtros, más dos ventanas
modales (formulario y detalle). Todo el contenido depende de datos que solo
existen después de consultar la API y que cambian con cada acción del usuario. No
hay contenido público, no hay SEO que optimizar, no hay usuarios anónimos.

**Alternativas consideradas.**

| Alternativa | Qué aporta aquí | Qué cuesta |
|---|---|---|
| React + Vite | Build simple, resultado estático, arranque rápido | Sin renderizado en servidor |
| Next.js | SSR, rutas por archivos, optimización de imágenes | Servidor Node vivo en producción, más conceptos que dominar |

**Decisión.** React 19 con Vite, generando una SPA estática.

**Justificación.**
Ninguna de las capacidades distintivas de Next.js aplica a este problema. El
renderizado en servidor mejora el primer pintado de contenido público e
indexable; aquí no hay contenido que indexar y la primera pantalla útil depende
igual de una llamada a la API. El enrutado por archivos resuelve la complejidad
de decenas de rutas; aquí hay una. A cambio, Next.js obliga a mantener un proceso
Node ejecutándose en producción, lo que cambia por completo la contenerización:
en vez de servir archivos estáticos con Nginx habría que mantener vivo un
servidor, distinguir entre componentes de servidor y de cliente, y explicar por
qué el contenedor no puede simplemente apagarse tras el build. Ese es
precisamente el tipo de complejidad que la actividad pide evitar.

**Consecuencias.**

- ✅ El artefacto de producción son archivos estáticos: la imagen final es un
  Nginx de ~50 MB, sin Node ni `node_modules`.
- ✅ El build tarda segundos y el equipo entiende cada línea del Dockerfile.
- ⚠️ Si mañana se necesitara SEO o renderizado en servidor, habría que migrar. Se
  asume: para una herramienta interna es un escenario improbable.
- ⚠️ Al no haber enrutado, el detalle de una incidencia no tiene URL propia y no
  se puede compartir por link. Limitación aceptada y registrada.

---

## DA-02 — JavaScript en lugar de TypeScript

**Contexto.**
El frontend son diez archivos y un único módulo que habla con la API. El equipo
tiene experiencia despareja con TypeScript y el tiempo de la actividad es
acotado.

**Decisión.** JavaScript con JSX, sin capa de tipos.

**Justificación.**
El valor principal de TypeScript aparece cuando muchos módulos se pasan
estructuras entre sí y nadie recuerda la forma exacta de cada objeto. Aquí la
superficie de riesgo es una sola: la forma de las respuestas de la API. Esa
superficie se acotó de otra manera, que además resuelve un problema que
TypeScript *no* resuelve: todo el tráfico HTTP pasa por `src/api/cliente.js`, y
el contrato está documentado en `arquitectura.md`. TypeScript verifica lo que el
desarrollador *declara* que devuelve el servidor, no lo que el servidor devuelve
realmente; si el backend cambia un campo y nadie actualiza la interfaz, el
compilador no lo detecta igual. Adoptarlo habría significado además sumar
`tsconfig.json`, tipos de React y un paso de verificación que todo el equipo debe
saber mantener, a cambio de una garantía parcial sobre el único riesgo relevante.

**Consecuencias.**

- ✅ Menos configuración y un ciclo de edición más corto.
- ✅ Cualquier integrante puede modificar un componente sin pelear con tipos.
- ❌ **Desventaja real y asumida:** si el backend renombra `fechaCreacion`, el
  error no aparece al compilar sino en pantalla, como un "—" o un `undefined`.
  Se mitigó centralizando las llamadas en un módulo, usando encadenamiento
  opcional al leer respuestas y devolviendo valores por defecto en `formato.js`.
- ⚠️ Si el proyecto creciera a varias pantallas, esta decisión debería revisarse.
  Migrar después es más caro que haber partido con TypeScript.

---

## DA-03 — PostgreSQL en lugar de MySQL

**Contexto.**
Se necesita una base relacional para un modelo de dos tablas, con restricciones
de integridad, ejecutable en contenedor y con volumen persistente.

**Decisión.** PostgreSQL 16, imagen `postgres:16-alpine`.

**Justificación.**
Dos razones concretas, no de preferencia:

1. **Comparación de texto predecible.** La búsqueda usa `LOWER(campo) LIKE
   LOWER(patrón)`. En MySQL el resultado depende del *collation* de la tabla, que
   suele ser insensible a mayúsculas y puede enmascarar errores que aparecerían
   en otro entorno. En PostgreSQL el comportamiento es explícito y no depende de
   la configuración del servidor.
2. **Continuidad con el resto de la asignatura.** Las unidades siguientes apuntan
   a despliegue cloud native, donde PostgreSQL es el motor gestionado más
   frecuente en los proveedores principales. Aprender su operación ahora se
   reutiliza después.

**Cómo se conecta Spring Boot con la persistencia.**

```text
IncidenciaRepository (interfaz que escribe el equipo)
        ↓  Spring Data JPA genera la implementación en tiempo de arranque
Spring Data JPA
        ↓
Hibernate 7 (proveedor JPA: traduce entidades y Criteria a SQL)
        ↓
HikariCP (pool de conexiones)
        ↓
Driver JDBC de PostgreSQL
        ↓
PostgreSQL 16
```

El mapeo se declara con anotaciones JPA sobre las entidades (`@Entity`,
`@Table`, `@Column`, `@Enumerated`, `@OneToMany`). Los enums se guardan como
texto con `@Enumerated(EnumType.STRING)` y no como número ordinal: si mañana
alguien reordena las constantes del enum, los datos guardados seguirían
significando lo mismo. Con `ORDINAL`, insertar un valor nuevo en medio del enum
corrompería silenciosamente todas las filas existentes.

**Consecuencias.**

- ✅ Las reglas críticas quedan protegidas por el código de la entidad (ver
  DA-04); la base, al crearla Hibernate, no agrega restricciones `CHECK`.
- ✅ Alineado con lo que viene en la asignatura.
- ⚠️ La base no publica puerto en el host: para inspeccionarla se usa
  `docker compose exec database psql ...`. Así no choca con un PostgreSQL
  instalado en la máquina de algún integrante.
- ⚠️ Guardar enums como texto ocupa más que un `smallint`. Irrelevante a esta
  escala; se prefiere la legibilidad al consultar directo con `psql`.

---

## DA-04 — Organización del backend por capas, con las reglas en el dominio

**Contexto.**
Hay que decidir dónde vive cada cosa, de manera que cualquier integrante ubique
en segundos dónde se agrega un endpoint, dónde se valida un dato o dónde se
consulta la base.

**Decisión.** Capas clásicas —`controller`, `service`, `repository`— más
`models/entities`, `dto`, `mapper` y `exception`. Las **invariantes de negocio
viven en las entidades del dominio**, no en el servicio.

```text
controller         →  HTTP: rutas, verbos, códigos de estado. Cero lógica de negocio.
service            →  Transacciones, búsqueda de entidades, 404, orquestación, mapeo.
models/entities    →  Reglas e invariantes: estado inicial, transiciones, historial.
repository         →  Acceso a datos y filtro dinámico.
dto                →  Contrato de la API + anotaciones de validación.
mapper             →  Entidad ⇄ DTO.
exception          →  Excepciones propias + @RestControllerAdvice.
```

**Justificación.**
La parte estándar de la decisión es la separación en capas. La parte que hubo que
decidir es dónde poner las reglas de negocio. La alternativa habitual es dejar la
entidad como un contenedor de *getters* y *setters* y escribir toda la lógica en
el servicio. Se descartó por un motivo verificable: si `Incidencia` tuviera un
`setEstado()` público, cualquier código futuro podría cambiar el estado sin pasar
por la validación de transición y sin registrar el historial, y nada lo
impediría. Al no existir ese *setter* y exponer solo `cambiarEstado(nuevo,
comentario)`, la regla es **imposible de saltar**, no solo "está escrita en algún
lado".

Que las reglas vivan en la entidad es además lo que mantiene la coherencia del
dato aunque no haya restricciones `CHECK` en la base: como la entidad es la única
forma de crear, modificar o transicionar una incidencia, y la base solo persiste
lo que ella produce, no hay un camino que omita la validación.

**Consecuencias.**

- ✅ Las reglas no se pueden evadir por accidente desde código nuevo.
- ✅ El servicio queda corto y legible: se lee como una lista de casos de uso.
- ⚠️ Es un paso más allá del CRUD anémico habitual; requiere explicar por qué la
  entidad tiene métodos con nombre de acción y no *setters*.
- ⚠️ La entidad de dominio depende de una excepción propia. Es una dependencia
  hacia una clase del propio proyecto, sin acoplamiento a Spring ni a JPA.
- ⚠️ Aún no hay pruebas unitarias de estas reglas: la única prueba automatizada
  es de arranque del contexto (`BackendApplicationTests`). Escribir `IncidenciaTest`
  sería directo, porque las reglas son objetos Java puros que no requieren Spring.

---

## DA-05 — Estrategia de contenerización

**Contexto.**
Se debe poder levantar toda la solución en una máquina limpia con Docker, sin
instalar Java, Node ni PostgreSQL, y sin que los datos se pierdan al reiniciar.

**Decisión.** Tres contenedores, dos construidos con *build multi-stage* y uno
tomado de una imagen oficial; volumen con nombre para la base; red por defecto
de Compose; el backend espera a que la base esté sana.

### Cómo se construye el backend

```text
código fuente (src/)
     ↓  etapa 1: maven:3.9.11-eclipse-temurin-21
build con Maven  →  descarga de dependencias cacheada aparte
     ↓
target/backend-0.0.1-SNAPSHOT.jar   (uber-jar de Spring Boot)
     ↓  etapa 2: eclipse-temurin:21-jre
java -jar app.jar   →  Tomcat embebido en :8080
```

Dos etapas porque compilar y ejecutar necesitan cosas distintas. La primera
requiere el JDK completo y Maven; la segunda solo un JRE. Separarlas deja una
imagen final sin código fuente, sin Maven y sin las dependencias de compilación.

El `pom.xml` se copia **antes** que `src/` a propósito: mientras el `pom.xml` no
cambie, Docker reutiliza la capa con las dependencias ya descargadas y una
modificación en el código no obliga a bajar Maven Central de nuevo. La descarga
de dependencias se prepara además con `dependency:go-offline` en una capa propia.

Las pruebas **no** se ejecutan al construir la imagen (`-DskipTests`).
El objetivo de este paso es empaquetar un artefacto; verificar que el código es
correcto es responsabilidad del entorno de desarrollo y del pipeline de CI. Es
una decisión discutible y se asume conscientemente: una imagen puede construirse
con pruebas rotas.

### Cómo se construye el frontend

```text
código fuente (src/)
     ↓  etapa 1: node:22-alpine
npm ci + vite build   →  dist/ (HTML, JS y CSS con hash en el nombre)
     ↓  etapa 2: nginx:1.27-alpine
Nginx sirve dist/ en :80 y hace proxy de /api hacia backend:8080
```

Aquí está la diferencia clave con Next.js: con Vite el resultado del build son
archivos estáticos, así que **nada de Node queda ejecutándose**. Node solo existe
en la etapa de construcción y desaparece de la imagen final. Con Next.js, en
cambio, habría que mantener vivo el proceso Node que resuelve el renderizado en
cada petición, y la segunda etapa sería una imagen de Node, no de Nginx.

`npm ci` en lugar de `npm install` porque instala exactamente las versiones que
fija `package-lock.json`: el build es reproducible en cualquier máquina y en
cualquier fecha. No se pasa ninguna variable en tiempo de build: el bundle se
compila contra la ruta relativa `/api` y Nginx la resuelve (ver DA-06).

### Cómo se ejecuta la base de datos

Imagen oficial `postgres:16-alpine` sin Dockerfile propio. No hay script de
inicialización de la imagen: el esquema lo crea **Hibernate** al arrancar el
backend con `ddl-auto: update`. La creación del esquema no está versionada; es
una deuda asumida y explicada en DA-07.

### Cómo se comunican los servicios

Los tres contenedores comparten la red que Compose crea por defecto. Docker
levanta un DNS interno donde el nombre del servicio resuelve a la IP del
contenedor:

```text
backend  →  jdbc:postgresql://database:5432/helpdesk  ("database" = nombre del servicio)
nginx    →  proxy_pass http://backend:8080/api/       ("backend" = nombre del servicio)
```

Usar `localhost` en cualquiera de los dos casos sería un error: dentro del
contenedor del backend, `localhost` es el propio backend.

El arranque del backend se encadena con `depends_on` y
`condition: service_healthy`: Postgres debe responder a `pg_isready` antes de
arrancar, evitando que el backend intente conectarse mientras el clúster aún
inicializa. El frontend depende de `backend` sin condición de salud; si el
backend tarda en levantar, las primeras llamadas a `/api` devuelven 502 hasta
que esté listo.

### Cómo se preservan los datos

El volumen con nombre `postgres_data` se monta en
`/var/lib/postgresql/data`. Los datos viven en el volumen y no en la capa
escribible del contenedor, así que `docker compose down` y un nuevo
`docker compose up` los conservan. Solo `docker compose down -v` elimina el
volumen, y con él los datos.

**Consecuencias.**

- ✅ Un solo comando levanta todo; funciona igual en cualquier máquina con Docker.
- ✅ Imágenes finales pequeñas y sin herramientas de compilación.
- ✅ Los datos sobreviven a reinicios, que es lo que se verifica en la revisión.
- ⚠️ El primer `--build` tarda varios minutos porque descarga las dependencias de
  Maven y npm. Los siguientes son mucho más rápidos gracias al orden de las capas.
- ⚠️ Sin CI, nada garantiza que las pruebas hayan pasado antes de construir.

---

## DA-06 — Proxy de la API en Nginx (y en el dev server de Vite) en lugar de llamadas directas con CORS

**Contexto.**
El frontend necesita llamar al backend. Con contenedores separados y puertos
distintos, la opción evidente —`fetch('http://localhost:8080/api/...')`— convierte
cada llamada en una petición entre orígenes distintos.

**Alternativas consideradas.**

1. El navegador llama directo a `http://localhost:8080/api` y el backend habilita
   CORS para el origen del sitio.
2. Un proxy recibe `/api` en el mismo origen del sitio y lo reenvía al backend.
   En producción lo hace Nginx; durante el desarrollo local lo hace el propio
   dev server de Vite (`vite.config.js` reenvía `/api` a `http://localhost:8080`).

**Decisión.** Opción 2 como único mecanismo. No se configuró CORS en el backend:
con los dos proxies no hay ninguna petición entre orígenes distintos, así que la
configuración no haría falta en ningún flujo real.

**Justificación.**
Con la opción 1, la URL del backend quedaría incrustada en el bundle de
JavaScript al momento de compilar. Como Vite resuelve las variables `VITE_*` en
tiempo de build, publicar el mismo frontend en otra máquina o con otro puerto
exigiría reconstruir la imagen. Con el proxy, el frontend solo conoce la ruta
relativa `/api` y funciona igual sin importar dónde esté publicado. Además, cada
petición entre orígenes distintos agrega una llamada `OPTIONS` previa que el
proxy evita.

En desarrollo el proxy del dev server resuelve el mismo problema de orígenes sin
necesitar CORS: el navegador habla solo con `localhost:5173` y Vite reenvía al
backend en `localhost:8080`. Esto se eligió por encima de habilitar CORS porque
no requiere tocar el backend ni mantener una lista de orígenes permitidos.

**Consecuencias.**

- ✅ El mismo bundle sirve en cualquier entorno; no hay URL de backend quemada.
- ✅ Sin peticiones `OPTIONS` previas en el uso normal.
- ✅ El backend no necesita configuración CORS ni variables asociadas.
- ⚠️ Nginx pasa a ser una pieza más que el equipo debe entender.
- ⚠️ En producción, `frontend` depende de `backend` sin condición de salud: si el
  backend tarda en arrancar, las primeras llamadas a `/api` devuelven 502 hasta
  que esté listo. La guía de verificación incluye este caso.

---

## DA-07 — Esquema creado por Hibernate en lugar de migraciones versionadas

**Contexto.**
Hay que crear las tablas en algún momento. Spring Boot ofrece atajos:
`ddl-auto: create` genera el esquema desde las entidades en cada arranque;
`update` intenta ajustarlo incrementalmente; Flyway lo crea con scripts
versionados.

**Decisión.** Hibernate crea el esquema con `ddl-auto: update` (configurado con
la variable `SPRING_JPA_HIBERNATE_DDL_AUTO` en Compose). No hay migraciones
versionadas.

**Justificación.**
`ddl-auto: create` borra los datos en cada arranque, lo que contradice
directamente el requisito de persistencia (RNF-02) que se va a verificar en la
revisión. `update` conserva los datos y agrega columnas nuevas, que es todo lo
que esta etapa necesita: un solo equipo, una sola base y sin instancias de
producción. Las migraciones versionadas agregan valor sobre todo cuando el
esquema debe evolucionar frente a bases existentes y a varios entornos
(desarrollo, staging, producción) — escenarios que este diagnóstico no tiene.
Se prefirió la velocidad de desarrollo.

Se asume la contraparte de forma consciente: al no haber restricciones `CHECK`,
las reglas de negocio (RN-03, RN-07) dependen exclusivamente del código de la
entidad, que es la única puerta de entrada a los datos (ver DA-04).

**Consecuencias.**

- ✅ Cero fricción al agregar un campo: se toca la entidad y nada más.
- ✅ `update` no borra datos, así que RNF-02 se cumple.
- ❌ **Desventaja real y asumida:** el esquema no está versionado. El esquema
  real puede desviarse en silencio del que uno cree tener, y no hay forma de
  reproducir un esquema exacto en otra base sin re-crearla desde cero.
- ❌ No hay restricciones `CHECK` a nivel de base: la coherencia de
  `fecha_resolucion` y los valores de catálogo dependen del código, no de la
  base de datos.
- ⚠️ Al incorporar despliegues a producción, la deuda se paga adoptando Flyway
  con `ddl-auto: validate`; no se puede dejar como está indefinidamente.

---

## DA-08 — DTOs propios en lugar de exponer las entidades

**Contexto.**
La forma más rápida de responder JSON es devolver la entidad JPA directamente
desde el controlador.

**Decisión.** La API expone `record` de entrada y salida; la entidad no sale
nunca del backend.

**Justificación.**
Devolver la entidad acopla el contrato HTTP al modelo de datos: renombrar una
columna rompería a todos los clientes. También expone campos que no corresponden
—la relación `historial` viajaría en cada elemento del listado— y con relaciones
perezosas puede provocar errores de serialización al intentar leerlas fuera de la
transacción. Los DTO de entrada aportan algo adicional: `CrearIncidenciaRequest`
sencillamente **no tiene** campo `estado`, de modo que RN-01 no es una validación
que pueda olvidarse, sino algo que el contrato hace imposible de enviar.

Se usaron `record` de Java porque son inmutables y no necesitan *getters*,
*setters*, `equals` ni `hashCode` escritos a mano.

**Consecuencias.**

- ✅ El contrato de la API evoluciona sin arrastrar al modelo de datos.
- ✅ RN-01 queda garantizada por el diseño del contrato.
- ⚠️ Hay una clase de mapeo que mantener (`IncidenciaMapper`). A esta escala es
  código trivial; se prefirió escribirlo a mano antes que sumar MapStruct.

---

## DA-09 — Formato único de error con `@RestControllerAdvice`

**Contexto.**
Sin manejo explícito, un error de validación devuelve el formato por defecto de
Spring, un id inexistente devuelve un 500 o una traza, y una excepción no
controlada devuelve una página de error HTML. Tres formatos distintos para un
frontend que debe reaccionar a los tres.

**Decisión.** Un `@RestControllerAdvice` traduce cada tipo de excepción a un
mismo cuerpo JSON con seis claves fijas, distinguiendo 400 (dato mal formado),
404 (recurso inexistente), 409 (regla de negocio) y 500 (imprevisto).

**Justificación.**
Un único formato permite un único camino de manejo de errores en el frontend:
`cliente.js` lee `mensaje` para el aviso general y `detalles` para pintar el
error bajo cada campo, sin saber qué salió mal. La separación entre 400 y 409
tampoco es cosmética: comunica al cliente si el problema se corrige cambiando el
dato enviado (400) o cambiando la operación que intenta (409). Un título de dos
caracteres es un 400; intentar reabrir una incidencia resuelta es un 409, porque
el dato está perfecto y lo que no corresponde es la acción.

**Consecuencias.**

- ✅ El frontend maneja errores en un solo lugar.
- ✅ Los mensajes están en español y son legibles por una persona.
- ✅ Las trazas quedan en el log del servidor, no en la respuesta HTTP.
- ⚠️ Hay que recordar agregar el manejador cuando aparezca una excepción nueva; el
  `@ExceptionHandler(Exception.class)` actúa como red de seguridad con 500.

---

## DA-10 — Filtro dinámico con Specifications

**Contexto.**
El listado combina cuatro filtros opcionales —estado, prioridad, categoría y
texto— más cuatro criterios de ordenamiento. Con métodos derivados de Spring Data
harían falta decenas de combinaciones.

**Alternativas consideradas.**

| Alternativa | Problema |
|---|---|
| Un método derivado por combinación | Explosión combinatoria inmantenible |
| `@Query` con `:param IS NULL OR campo = :param` | Consulta ilegible; parámetros nulos con tipo enum dan problemas |
| Concatenar SQL según los filtros presentes | Riesgo de inyección SQL |
| Specifications (Criteria API) | Más verboso, pero componible y con parámetros preparados |

**Decisión.** `JpaSpecificationExecutor` con un constructor de predicados en
`IncidenciaSpecs`; cada parámetro nulo simplemente no aporta condición.

**Justificación.**
Agregar un filtro nuevo es agregar un `if` de tres líneas, no una consulta más.
La Criteria API genera siempre sentencias con parámetros preparados, así que la
inyección SQL queda descartada por construcción y no por disciplina. El
ordenamiento se resuelve aparte con `Sort`, y el criterio llega como un `enum`
(`Orden`), de modo que un valor inválido lo rechaza Spring con un 400 antes de
tocar la base de datos, en lugar de viajar como texto hacia una consulta.

**Consecuencias.**

- ✅ Un solo `WHERE` dinámico para todas las combinaciones.
- ✅ Sin riesgo de inyección SQL.
- ⚠️ La Criteria API es más difícil de leer que una consulta JPQL. Se acotó
  aislándola en una clase pequeña y comentada.
- ⚠️ El ordenamiento por prioridad obliga a mantener la columna
  `nivel_prioridad`, porque ordenar el enum guardado como texto daría ALTA,
  BAJA, MEDIA. La sincronización es automática vía `@PrePersist` / `@PreUpdate`.

---

## DA-11 — Estrategia de trabajo con Git

**Contexto.**
Varios integrantes trabajando en paralelo sobre un proyecto pequeño y de corta
duración.

**Decisión.** Rama `main` protegida, ramas cortas por funcionalidad con el
formato `tipo/descripcion-breve` e integración mediante *pull request* con
revisión de al menos otro integrante.

```text
main
 ├── feat/entidad-incidencia
 ├── feat/endpoints-crud
 ├── feat/listado-frontend
 ├── feat/docker-compose
 └── docs/requerimientos
```

Mensajes de commit en español, en imperativo y describiendo el *qué*:

```text
feat: agregar validación de transiciones de estado
fix: corregir orden por prioridad usando nivel numérico
docs: documentar decisión sobre Vite y Next.js
chore: agregar .env.example y actualizar .gitignore
```

**Justificación.**
Ramas cortas por funcionalidad reducen los conflictos, que en este proyecto se
concentrarían en `App.jsx` y en `compose.yaml`. El *pull request* obliga a que al
menos dos personas hayan leído cada cambio, lo que importa especialmente cuando
después cada integrante debe poder explicar la solución completa de forma
individual. Un flujo más elaborado (GitFlow con `develop`, `release` y `hotfix`)
resolvería problemas de versionado y despliegue que este proyecto no tiene.

**Consecuencias.**

- ✅ Historial legible con contribuciones distribuidas y atribuibles.
- ✅ Nadie integra código que otro no haya leído.
- ⚠️ Requiere disciplina para no acumular ramas largas; con ramas de varios días
  el conflicto vuelve a aparecer.
