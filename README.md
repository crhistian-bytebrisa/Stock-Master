# Stock-Master

API REST para la gestión de inventario (productos, repuestos, sucursales,
traspasos, órdenes de restock, etc.) construida con Spring Boot 4 y Java 21.

## Stack de observabilidad

- **OpenTelemetry**: la API exporta trazas OTLP hacia Tempo.
- **Prometheus**: guarda las métricas expuestas por `/actuator/prometheus`.
- **Loki**: recibe los logs de la API (appender Logback).
- **Grafana**: gráficos sobre Prometheus y Loki (dashboard `Stock Master - Observabilidad`).
- **Tempo**: almacena las trazas distribuidas.

## Ejecución local

Desde la carpeta `stock-master`:

```bash
docker compose up --build -d
```

Este comando levanta PostgreSQL, la API, Prometheus, Grafana, Loki y Tempo.

- API: http://localhost:8080
- Métricas de la API: http://localhost:8080/actuator/prometheus
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (usuario y contraseña: `admin`)
- Loki: http://localhost:3100/ready
- Tempo: http://localhost:3200

Los puertos y credenciales se pueden cambiar con las variables
`APP_PORT`, `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER`,
`POSTGRES_PASSWORD`, `PROMETHEUS_PORT`, `GRAFANA_PORT`, `GRAFANA_ADMIN_USER`,
`GRAFANA_ADMIN_PASSWORD`, `LOKI_PORT`, `TEMPO_PORT` y
`TEMPO_OTLP_HTTP_PORT`.

Para detener el entorno:

```bash
docker compose down
```

Para eliminar también los datos persistidos de PostgreSQL:

```bash
docker compose down -v
```

El dashboard provisionado en Grafana incluye:

- Peticiones HTTP por método (GET, POST, PUT, DELETE).
- Peticiones por código de estado (200, 201, 400, 500, ...).
- Peticiones por servicio/endpoint (`/api/productos`, `/api/traspasos`, ...).
- Conteo de registros por tabla en la base de datos.
- Logs de las peticiones (Loki).
- Trazas de la API (Tempo): tabla de búsqueda, waterfall y grafo de spans.

## Trazas (Tempo)

Una traza es el recorrido completo de una petición, partido en **spans**. Cada
span tiene un `spanId` y un `parentSpanId`, y el árbol de padres e hijos es lo que
Grafana dibuja como waterfall y como grafo.

### Cómo funciona el circuito

```
petición HTTP
  └─ Spring Boot crea el span raíz
       └─ micrometer-tracing lo registra en el ObservationRegistry
            └─ el SDK de OpenTelemetry decide si se guarda (sampling)
                 └─ opentelemetry-exporter-otlp lo manda a tempo:4318/v1/traces
                      └─ Tempo lo guarda y Grafana lo consulta con TraceQL
```

La API ya lo tiene montado:

- `pom.xml`: `spring-boot-starter-opentelemetry`.
- `application.properties`: `management.tracing.sampling.probability=1.0`
  (se guarda el 100% de las peticiones) y
  `management.tracing.export.otlp.enabled=false` por defecto.
- `compose.yml`: en Docker se sobreescribe a `true` y se apunta a
  `http://tempo:4318/v1/traces`.

### Los tres paneles de trazas

| Panel | Query | Qué dibuja |
| --- | --- | --- |
| Tabla de trazas | `queryType: traceql` + `tableType: traces` | Lista las trazas de la API |
| Waterfall | `queryType: traceId`, `query: $traza` | La cascada de spans de esa traza |
| Node graph | `queryType: traceId`, `query: $traza` | Los mismos spans como nodos y flechas |

Para usarlos: hacé click en una fila de la tabla y el waterfall y el grafo se
cargan con esa traza. También podés pegar un ID a mano en la variable
`Trace ID a inspeccionar` de arriba.

### Dos detalles que no son obvios

1. **El filtro `/api/.*` es obligatorio.** Prometheus scrapea
   `/actuator/prometheus` cada 15 segundos, así que sin el filtro 19 de cada 20
   trazas de la tabla son el scrape, no las peticiones de la API.

2. **El waterfall y el grafo no pueden usar una búsqueda**, solo un `traceId`.
   El panel Traces de Grafana descarta cualquier resultado que no tenga `spanID`
   por fila y muestra "No data found in response". Por eso existe la tabla: es
   la que elige qué traza mirar.

3. **El node graph depende de la datasource, no del panel.** En
   `observability/grafana/provisioning/datasources/prometheus.yml` la datasource
   Tempo tiene `jsonData.nodeGraph.enabled: true`. Sin ese flag, Grafana no
   transforma la traza a nodos y aristas aunque el panel sea correcto.

### Limitación actual: un solo span por traza

Cada traza tiene **un único span** (el de Spring Boot), porque las consultas SQL
no están instrumentadas. El waterfall muestra una sola barra y el grafo un solo
nodo. Para ver las queries SQL dentro de las trazas hay que agregar
`net.ttddyy:datasource-proxy` al `pom.xml` (no viene en el BOM de Spring Boot,
así que hay que fijar la versión) y activarlo con:

```properties
spring.jpa.properties.hibernate.session_factory.interceptor=net.ttddyy.dsproxy.support.ProxyDataSourceAdvisor
```

No se agregó a propósito para no agregar dependencias sin gestionar por el BOM a
un proyecto que se usa para estudiar.

## Cómo integrar trazas en otra API

Son cuatro cosas, en este orden.

### 1. Dependencias

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-opentelemetry</artifactId>
</dependency>
```

Eso trae el SDK de OpenTelemetry, el bridge de Micrometer y el exportador OTLP.
No hace falta declarar `micrometer-tracing` ni `opentelemetry-exporter-otlp` por
separado: vienen por transitividad.

### 2. Configuración

```properties
spring.application.name=mi-api
management.tracing.sampling.probability=1.0
management.tracing.export.otlp.enabled=true
management.opentelemetry.tracing.export.otlp.endpoint=http://tempo:4318/v1/traces
```

`spring.application.name` es importante: es el `service.name` que aparece en
Tempo y en el `resource.service.name` de las consultas TraceQL.

El `sampling.probability` es la palanca de costo. Con `1.0` se guarda todo. En
producción se suele bajar a `0.1` (una de cada diez) y atributos de interes
(latencia alta, errores) se muestrean siempre con `tracing.sampling.priority`.

### 3. Variables de entorno en el compose

Como en local no hay receptor OTLP, se deja exportando apagado por defecto y se
enciende solo en Docker:

```yaml
environment:
  MANAGEMENT_TRACING_EXPORT_OTLP_ENABLED: "true"
  MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_ENDPOINT: http://tempo:4318/v1/traces
```

Spring Boot traduce esos nombres de variables a las properties de arriba.

### 4. Provisionar Tempo en Grafana

Agregar la datasource en `observability/grafana/provisioning/datasources/`:

```yaml
- name: Tempo
  uid: tempo
  type: tempo
  access: proxy
  url: http://tempo:3200
  editable: false
  jsonData:
    nodeGraph:
      enabled: true
```

El `nodeGraph.enabled` no es opcional si querés usar el panel de grafo.

Y en el panel de Prometheus conviene dejar el enlace de exemplars, para poder
saltar de una métrica a la traza:

```yaml
jsonData:
  exemplarTraceIdDestinations:
    - name: trace_id
      datasourceUid: tempo
```

### Checklist para verificar que anda

1. `curl localhost:3200/api/search?start=<epoch segundos>&end=<epoch segundos>`
   tiene que devolver trazas. Ojo: **epoch en segundos**, no en milisegundos ni
   nanosegundos; con nanosegundos Tempo responde
   `invalid start: strconv.ParseUint ... value out of range`.
2. Filtrar por `service.name` y confirmar que aparece la API y no solo
   `/actuator/prometheus`.
3. En Grafana, la tabla de trazas tiene que listar filas.

Con eso ya se puede agregar la API al mismo Grafana: cada una con su
`spring.application.name` distinto, y las consultas TraceQL distinguen por
`resource.service.name`.

## Documentación

- `stock-master/HELP.md`: referencias y guías de Spring Boot/Maven.
- La API expone sus controladores bajo `stock-master/src/main/java`.
