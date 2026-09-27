# Stock-Master
Proyecto hecho para demostrar lo potent que pueden ser los logs a la hora de trabajar en sistemas.

## Ejecución local

Desde la carpeta `stock-master`:

```bash
docker compose up --build -d
```

Este comando levanta PostgreSQL, la API, Loki, Grafana y el simulador Python.
El simulador envía automáticamente sus eventos JSON a Loki.

- API: http://localhost:8080
- Métricas del simulador: http://localhost:8000/metrics
- Loki: http://localhost:3100/ready
- Grafana: http://localhost:3000 (usuario y contraseña: `admin`)

Grafana incluye el datasource Loki y el dashboard **Stock Master - Logs** ya
aprovisionados. Para seguir los eventos desde la terminal:

```bash
docker compose logs -f simulator
```

Los puertos, credenciales y frecuencia se pueden cambiar con las variables
`APP_PORT`, `POSTGRES_PORT`, `METRICS_PORT`, `LOKI_PORT`, `GRAFANA_PORT`,
`GRAFANA_ADMIN_USER`, `GRAFANA_ADMIN_PASSWORD` y `SIMULATOR_INTERVAL`.

Para ejecutar además el script de llamadas de demostración contra la API:

```bash
python3 scripts/demo_api.py
```

El script utiliza únicamente la biblioteca estándar de Python 3. Para detener el entorno:

```bash
docker compose down
```

Para eliminar también los datos persistidos de PostgreSQL:

```bash
docker compose down -v
```
