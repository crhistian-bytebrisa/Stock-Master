# Stock-Master
Proyecto hecho para demostrar lo potent que pueden ser los logs a la hora de trabajar en sistemas.

## Ejecución local

Desde la carpeta `stock-master`:

```bash
docker compose up --build -d
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
