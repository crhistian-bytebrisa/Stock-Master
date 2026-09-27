# simulate_demo.py — mini doc

Simulador cíclico de compras/traspasos entre `SUC-001..SUC-005` para alimentar
una demo en vivo de **Prometheus + Loki + Grafana**.

## Instalación

```bash
pip install prometheus_client requests
```

## Modos de operación

### 1. Local (default) — no toca ninguna API
El stock se calcula en memoria. Útil si no tienes backend levantado o no
quieres depender de él durante la demo.

```bash
python simulate_demo.py --interval 2 --metrics-port 8000 \
  --loki-url http://localhost:3100/loki/api/v1/push
```

### 2. API real — pega HTTP contra tu backend
Se activa solo con `--api-base-url`. Cada "compra" y "traspaso" hace un
`POST` real, y las métricas/logs salen de la respuesta (o de un fallback en
memoria si tu API no responde el campo esperado).

```bash
python simulate_demo.py --interval 2 --metrics-port 8000 \
  --loki-url http://localhost:3100/loki/api/v1/push \
  --api-base-url https://api.tuempresa.com \
  --api-token "eyJhbGciOi..." \
  --purchase-path /api/v1/purchases \
  --transfer-path /api/v1/transfers
```

## Contrato de endpoints esperado (modo API real)

| Acción   | Método | Path (default)       | Body                                                                 | Response esperado (opcional)                          |
|----------|--------|-----------------------|-----------------------------------------------------------------------|---------------------------------------------------------|
| Compra   | POST   | `/api/v1/purchases`   | `{"store":"SUC-001","product":"SKU-1001","amount":5}`                | `{"stock_after": 37}`                                    |
| Traspaso | POST   | `/api/v1/transfers`   | `{"origin":"SUC-001","destination":"SUC-002","product":"SKU-1001","amount":3}` | `{"stock_origin_after":34,"stock_destination_after":20}` |

Si tu API responde con otros nombres de campo, edita las dos líneas
`body.get(...)` dentro de `do_purchase_api` / `do_transfer_api` en el script.
Si tus rutas son distintas, usa `--purchase-path` / `--transfer-path` (no
hace falta tocar código).

Auth: se manda `Authorization: Bearer <token>` si pasas `--api-token`. Si tu
API usa otro esquema (API key en header, básica, etc.), edita el dict
`headers` dentro de `main()`.

## Flags disponibles

| Flag              | Default                | Descripción                                      |
|-------------------|-------------------------|---------------------------------------------------|
| `--interval`       | `2.0`                    | Segundos entre cada evento simulado               |
| `--metrics-port`   | `8000`                   | Puerto donde se expone `/metrics`                 |
| `--loki-url`       | *(ninguno)*              | Si se pasa, empuja logs directo a Loki            |
| `--api-base-url`   | *(ninguno)*              | Si se pasa, activa el modo API real               |
| `--api-token`      | *(ninguno)*              | Bearer token para la API real                     |
| `--purchase-path`  | `/api/v1/purchases`      | Path del endpoint de compras                      |
| `--transfer-path`  | `/api/v1/transfers`      | Path del endpoint de traspasos                    |
| `--api-timeout`    | `3.0`                    | Timeout por request a la API real (segundos)      |

## Métricas Prometheus expuestas

- `demo_purchases_total{store,product}` — contador de compras
- `demo_transfers_total{origin,destination,product}` — contador de traspasos
- `demo_low_stock_events_total{store,product}` — veces que cayó bajo el mínimo
- `demo_stock_level{store,product}` — gauge con el nivel actual
- **Solo modo API real:**
  - `demo_api_requests_total{endpoint,method,status}` — requests por status HTTP
  - `demo_api_request_duration_seconds{endpoint}` — histograma de latencia
  - `demo_api_errors_total{endpoint,error_type}` — timeouts/errores de red

## Ejemplo de scrape config para Prometheus

```yaml
scrape_configs:
  - job_name: "demo-simulator"
    static_configs:
      - targets: ["localhost:8000"]
```

## Logs (Loki)

Cada evento se imprime como una línea JSON por stdout y, si pasaste
`--loki-url`, además se empuja directo a la Push API de Loki (no necesitas
Promtail). Si prefieres usar Promtail, simplemente scrapea el stdout del
proceso y omite `--loki-url`.

Eventos posibles: `purchase`, `transfer`, `low_stock`, `purchase_failed`,
`transfer_failed` (los dos últimos solo en modo API real, cuando el backend
responde error o no responde).

## Grafana

Sugerencia rápida de panel: gauge de `demo_stock_level` por tienda, un
contador de `demo_low_stock_events_total`, y un panel de logs filtrando
`event="low_stock"` o `event=~".*_failed"` para ver fallos de API en vivo.