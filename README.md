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

## Documentación

- `stock-master/HELP.md`: referencias y guías de Spring Boot/Maven.
- La API expone sus controladores bajo `stock-master/src/main/java`.
