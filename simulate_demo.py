#!/usr/bin/env python3
"""
simulate_demo.py
-----------------
Generador ciclico de eventos para alimentar una demo de Grafana + Loki + Prometheus.

Simula, cada N segundos, una de estas acciones sobre 5 tiendas (SUC-001..SUC-005):
  - compra (purchase)      -> baja stock de una tienda
  - traspaso (transfer)    -> mueve stock entre dos tiendas
  - caída bajo el mínimo   -> se detecta sola cuando el stock queda por debajo
                               del umbral configurado y se loguea/cuenta aparte

Dos modos de operacion:
  - LOCAL (default): el stock se calcula en memoria, no toca ninguna API.
  - API REAL: si pasas --api-base-url, cada accion hace un request HTTP real
    (POST /purchases, POST /transfers) contra tu backend, y las metricas/logs
    salen de la respuesta real de la API (incluye status codes, latencia y
    errores 4xx/5xx como señales propias). Ver README_simulate_demo.md para
    el contrato de endpoints esperado y cómo adaptarlo al tuyo.

Expone:
  - Métricas Prometheus en http://localhost:<PORT>/metrics
  - Logs JSON estructurados por stdout (para Promtail) y opcionalmente
    empujados directo a Loki vía su Push API (sin necesidad de Promtail).

Requisitos:
  pip install prometheus_client requests

Uso típico (modo local, sin API):
  python simulate_demo.py --interval 2 --metrics-port 8000 \
      --loki-url http://localhost:3100/loki/api/v1/push

Uso típico (modo API real):
  python simulate_demo.py --interval 2 --metrics-port 8000 \
      --loki-url http://localhost:3100/loki/api/v1/push \
      --api-base-url https://api.tuempresa.com \
      --api-token "eyJhbGciOi..."
"""

import argparse
import json
import random
import sys
import time
from datetime import datetime, timezone

from prometheus_client import start_http_server, Counter, Gauge, Histogram

try:
    import requests
except ImportError:
    requests = None


# ------------------------------------------------------------------ #
# Configuración base
# ------------------------------------------------------------------ #

STORES = [f"SUC-{i:03d}" for i in range(1, 6)]  # SUC-001..SUC-005
PRODUCTS = ["SKU-1001", "SKU-1002", "SKU-1003", "SKU-1004"]
MIN_STOCK = 10          # umbral mínimo por (tienda, producto)
INITIAL_STOCK_RANGE = (15, 60)

# ------------------------------------------------------------------ #
# Métricas Prometheus
# ------------------------------------------------------------------ #

PURCHASES_TOTAL = Counter(
    "demo_purchases_total", "Compras simuladas", ["store", "product"]
)
TRANSFERS_TOTAL = Counter(
    "demo_transfers_total", "Traspasos simulados entre tiendas",
    ["origin", "destination", "product"]
)
LOW_STOCK_EVENTS_TOTAL = Counter(
    "demo_low_stock_events_total", "Veces que el stock cayó bajo el mínimo",
    ["store", "product"]
)
STOCK_LEVEL = Gauge(
    "demo_stock_level", "Nivel de stock actual", ["store", "product"]
)

# Métricas propias del modo API real (llamadas HTTP contra el backend)
API_REQUESTS_TOTAL = Counter(
    "demo_api_requests_total", "Requests hechos a la API real",
    ["endpoint", "method", "status"]
)
API_REQUEST_DURATION = Histogram(
    "demo_api_request_duration_seconds", "Latencia de los requests a la API real",
    ["endpoint"]
)
API_ERRORS_TOTAL = Counter(
    "demo_api_errors_total", "Errores de red/timeout al llamar la API real",
    ["endpoint", "error_type"]
)

# ------------------------------------------------------------------ #
# Estado en memoria
# ------------------------------------------------------------------ #

def init_stock():
    stock = {}
    for store in STORES:
        for product in PRODUCTS:
            qty = random.randint(*INITIAL_STOCK_RANGE)
            stock[(store, product)] = qty
            STOCK_LEVEL.labels(store=store, product=product).set(qty)
    return stock


# ------------------------------------------------------------------ #
# Logging -> stdout JSON + push opcional a Loki
# ------------------------------------------------------------------ #

def log_event(level, event, **fields):
    entry = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "level": level,
        "event": event,
        **fields,
    }
    line = json.dumps(entry, ensure_ascii=False)
    print(line, flush=True)
    return entry


def push_to_loki(loki_url, entry):
    if not loki_url or requests is None:
        return
    ts_ns = str(int(time.time() * 1e9))
    payload = {
        "streams": [
            {
                "stream": {
                    "job": "demo-simulator",
                    "event": entry["event"],
                    "level": entry["level"],
                },
                "values": [[ts_ns, json.dumps(entry, ensure_ascii=False)]],
            }
        ]
    }
    try:
        requests.post(loki_url, json=payload, timeout=2)
    except requests.RequestException as exc:
        print(f'{{"level":"error","event":"loki_push_failed","detail":"{exc}"}}',
              file=sys.stderr, flush=True)


# ------------------------------------------------------------------ #
# Acciones simuladas
# ------------------------------------------------------------------ #

def check_low_stock(store, product, qty, loki_url):
    if qty < MIN_STOCK:
        LOW_STOCK_EVENTS_TOTAL.labels(store=store, product=product).inc()
        entry = log_event(
            "warning", "low_stock",
            store=store, product=product, stock=qty, min_stock=MIN_STOCK,
        )
        push_to_loki(loki_url, entry)


def do_purchase(stock, loki_url):
    store = random.choice(STORES)
    product = random.choice(PRODUCTS)
    amount = random.randint(1, 8)

    stock[(store, product)] = max(0, stock[(store, product)] - amount)
    qty = stock[(store, product)]

    PURCHASES_TOTAL.labels(store=store, product=product).inc()
    STOCK_LEVEL.labels(store=store, product=product).set(qty)

    entry = log_event(
        "info", "purchase",
        store=store, product=product, amount=amount, stock_after=qty,
    )
    push_to_loki(loki_url, entry)
    check_low_stock(store, product, qty, loki_url)


def do_transfer(stock, loki_url):
    origin, destination = random.sample(STORES, 2)
    product = random.choice(PRODUCTS)
    available = stock[(origin, product)]
    amount = random.randint(1, min(6, max(1, available)))

    if available <= 0:
        amount = 0

    stock[(origin, product)] = max(0, available - amount)
    stock[(destination, product)] = stock[(destination, product)] + amount

    qty_origin = stock[(origin, product)]
    qty_dest = stock[(destination, product)]

    TRANSFERS_TOTAL.labels(origin=origin, destination=destination, product=product).inc()
    STOCK_LEVEL.labels(store=origin, product=product).set(qty_origin)
    STOCK_LEVEL.labels(store=destination, product=product).set(qty_dest)

    entry = log_event(
        "info", "transfer",
        origin=origin, destination=destination, product=product,
        amount=amount, stock_origin_after=qty_origin, stock_destination_after=qty_dest,
    )
    push_to_loki(loki_url, entry)
    check_low_stock(origin, product, qty_origin, loki_url)


ACTIONS = [do_purchase, do_purchase, do_transfer]  # compras más frecuentes que traspasos


# ------------------------------------------------------------------ #
# Modo API real: mismas acciones, pero contra un backend HTTP
# ------------------------------------------------------------------ #
#
# Contrato de endpoints que este modo asume por default (ajustelo con los
# flags --purchase-path / --transfer-path si su API usa otras rutas):
#
#   POST {api_base_url}{purchase_path}
#     body:  {"store": "SUC-001", "product": "SKU-1001", "amount": 5}
#     resp:  {"stock_after": 37}                <- opcional, si no viene
#                                                   se sigue el stock en memoria
#
#   POST {api_base_url}{transfer_path}
#     body:  {"origin": "SUC-001", "destination": "SUC-002",
#              "product": "SKU-1001", "amount": 3}
#     resp:  {"stock_origin_after": 34, "stock_destination_after": 20}  <- opcional
#
# Si la API responde distinto, solo hay que tocar los dos `resp_json.get(...)`
# de abajo para leer los campos correctos.

def _call_api(session, method, url, headers, timeout, json_body, endpoint_label):
    start = time.time()
    try:
        resp = session.request(method, url, json=json_body, headers=headers, timeout=timeout)
        API_REQUEST_DURATION.labels(endpoint=endpoint_label).observe(time.time() - start)
        API_REQUESTS_TOTAL.labels(
            endpoint=endpoint_label, method=method, status=str(resp.status_code)
        ).inc()
        return resp
    except requests.RequestException as exc:
        API_REQUEST_DURATION.labels(endpoint=endpoint_label).observe(time.time() - start)
        API_ERRORS_TOTAL.labels(endpoint=endpoint_label, error_type=type(exc).__name__).inc()
        return None


def do_purchase_api(stock, loki_url, api_cfg, session):
    store = random.choice(STORES)
    product = random.choice(PRODUCTS)
    amount = random.randint(1, 8)

    url = api_cfg["base_url"] + api_cfg["purchase_path"]
    resp = _call_api(session, "POST", url, api_cfg["headers"], api_cfg["timeout"],
                      {"store": store, "product": product, "amount": amount}, "purchase")

    if resp is not None and resp.ok:
        try:
            qty = resp.json().get("stock_after")
        except ValueError:
            qty = None
        if qty is None:  # la API no devolvió el campo esperado -> seguimos localmente
            stock[(store, product)] = max(0, stock[(store, product)] - amount)
            qty = stock[(store, product)]
        else:
            stock[(store, product)] = qty
        STOCK_LEVEL.labels(store=store, product=product).set(qty)
        entry = log_event("info", "purchase", store=store, product=product,
                           amount=amount, stock_after=qty, http_status=resp.status_code)
        push_to_loki(loki_url, entry)
        check_low_stock(store, product, qty, loki_url)
    else:
        status = resp.status_code if resp is not None else "no_response"
        entry = log_event("error", "purchase_failed", store=store, product=product,
                           amount=amount, http_status=status)
        push_to_loki(loki_url, entry)


def do_transfer_api(stock, loki_url, api_cfg, session):
    origin, destination = random.sample(STORES, 2)
    product = random.choice(PRODUCTS)
    amount = random.randint(1, 6)

    url = api_cfg["base_url"] + api_cfg["transfer_path"]
    resp = _call_api(session, "POST", url, api_cfg["headers"], api_cfg["timeout"],
                      {"origin": origin, "destination": destination,
                       "product": product, "amount": amount}, "transfer")

    if resp is not None and resp.ok:
        try:
            body = resp.json()
        except ValueError:
            body = {}
        qty_origin = body.get("stock_origin_after")
        qty_dest = body.get("stock_destination_after")
        if qty_origin is None or qty_dest is None:  # fallback local
            stock[(origin, product)] = max(0, stock[(origin, product)] - amount)
            stock[(destination, product)] = stock[(destination, product)] + amount
            qty_origin = stock[(origin, product)]
            qty_dest = stock[(destination, product)]
        else:
            stock[(origin, product)] = qty_origin
            stock[(destination, product)] = qty_dest

        STOCK_LEVEL.labels(store=origin, product=product).set(qty_origin)
        STOCK_LEVEL.labels(store=destination, product=product).set(qty_dest)
        entry = log_event("info", "transfer", origin=origin, destination=destination,
                           product=product, amount=amount, stock_origin_after=qty_origin,
                           stock_destination_after=qty_dest, http_status=resp.status_code)
        push_to_loki(loki_url, entry)
        check_low_stock(origin, product, qty_origin, loki_url)
    else:
        status = resp.status_code if resp is not None else "no_response"
        entry = log_event("error", "transfer_failed", origin=origin, destination=destination,
                           product=product, amount=amount, http_status=status)
        push_to_loki(loki_url, entry)


ACTIONS_API = [do_purchase_api, do_purchase_api, do_transfer_api]


# ------------------------------------------------------------------ #
# Main loop
# ------------------------------------------------------------------ #

def main():
    parser = argparse.ArgumentParser(description="Simulador de eventos para demo Grafana/Loki/Prometheus")
    parser.add_argument("--interval", type=float, default=2.0, help="Segundos entre eventos (default 2)")
    parser.add_argument("--metrics-port", type=int, default=8000, help="Puerto para /metrics (default 8000)")
    parser.add_argument("--loki-url", default=None,
                         help="URL Push API de Loki, ej http://localhost:3100/loki/api/v1/push. "
                              "Si no se pasa, solo se imprime JSON por stdout.")
    parser.add_argument("--api-base-url", default=None,
                         help="Si se pasa, deja de simular en memoria y pega HTTP real "
                              "contra este backend, ej https://api.tuempresa.com")
    parser.add_argument("--api-token", default=None,
                         help="Bearer token para la API real (header Authorization).")
    parser.add_argument("--purchase-path", default="/api/v1/purchases",
                         help="Path del endpoint de compras (default /api/v1/purchases)")
    parser.add_argument("--transfer-path", default="/api/v1/transfers",
                         help="Path del endpoint de traspasos (default /api/v1/transfers)")
    parser.add_argument("--api-timeout", type=float, default=3.0,
                         help="Timeout en segundos por request a la API real (default 3)")
    args = parser.parse_args()

    api_mode = args.api_base_url is not None
    if api_mode and requests is None:
        print('{"level":"error","event":"missing_dependency",'
              '"detail":"instala requests: pip install requests"}', file=sys.stderr)
        sys.exit(1)

    start_http_server(args.metrics_port)
    print(f'{{"level":"info","event":"startup","metrics_port":{args.metrics_port},'
          f'"loki_url":{json.dumps(args.loki_url)},"mode":"{"api" if api_mode else "local"}"}}',
          flush=True)

    stock = init_stock()

    if api_mode:
        session = requests.Session()
        headers = {"Content-Type": "application/json"}
        if args.api_token:
            headers["Authorization"] = f"Bearer {args.api_token}"
        api_cfg = {
            "base_url": args.api_base_url.rstrip("/"),
            "purchase_path": args.purchase_path,
            "transfer_path": args.transfer_path,
            "headers": headers,
            "timeout": args.api_timeout,
        }

    try:
        while True:
            if api_mode:
                action = random.choice(ACTIONS_API)
                action(stock, args.loki_url, api_cfg, session)
            else:
                action = random.choice(ACTIONS)
                action(stock, args.loki_url)
            time.sleep(args.interval)
    except KeyboardInterrupt:
        print('{"level":"info","event":"shutdown"}', flush=True)


if __name__ == "__main__":
    main()