#!/usr/bin/env python3
"""Ejecuta un caso de uso completo contra la API de Stock Master."""

from __future__ import annotations

import json
import os
import sys
import time
from datetime import date
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen


API_URL = os.getenv("API_URL", "http://localhost:8080").rstrip("/")
RUN_ID = os.getenv("RUN_ID", str(int(time.time())))
PRODUCT_ID = f"FILTRO-{RUN_ID}"


def request(method: str, path: str, body: dict[str, Any] | None = None) -> Any:
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Content-Type": "application/json"} if data is not None else {}
    http_request = Request(f"{API_URL}{path}", data=data, headers=headers, method=method)

    try:
        with urlopen(http_request, timeout=10) as response:
            content = response.read()
            return json.loads(content) if content else None
    except HTTPError as error:
        detail = error.read().decode(errors="replace")
        raise RuntimeError(f"{method} {path} devolvió HTTP {error.code}: {detail}") from error
    except URLError as error:
        raise RuntimeError(f"No fue posible conectar con {API_URL}: {error.reason}") from error


def post(path: str, body: dict[str, Any]) -> Any:
    return request("POST", path, body)


def wait_until_ready() -> None:
    print(f"Esperando Stock Master en {API_URL}...")
    for _ in range(30):
        try:
            health = request("GET", "/actuator/health")
            if health.get("status") == "UP":
                return
        except RuntimeError:
            pass
        time.sleep(2)
    raise RuntimeError("La API no respondió después de 60 segundos")


def run_demo() -> None:
    wait_until_ready()

    print("1/8 Creando sucursales")
    origin = post(
        "/api/sucursales",
        {
            "codigo": f"CENTRO-{RUN_ID}",
            "nombre": "Sucursal Centro",
            "direccion": "Av. Central 100",
            "activa": True,
        },
    )
    destination = post(
        "/api/sucursales",
        {
            "codigo": f"NORTE-{RUN_ID}",
            "nombre": "Sucursal Norte",
            "direccion": "Av. Norte 200",
            "activa": True,
        },
    )

    print(f"2/8 Creando producto {PRODUCT_ID}")
    post(
        "/api/productos",
        {
            "id": PRODUCT_ID,
            "nombre": "Filtro de aceite",
            "descripcion": "Repuesto para demostración",
            "precioCompra": 10.00,
            "precioVenta": 15.00,
        },
    )

    print("3/8 Inicializando inventario")
    post(
        "/api/inventarios",
        {"sucursalId": origin["id"], "productoId": PRODUCT_ID, "cantidad": 0},
    )
    post(
        "/api/inventarios",
        {"sucursalId": destination["id"], "productoId": PRODUCT_ID, "cantidad": 0},
    )

    print("4/8 Creando presupuesto")
    ppto = post("/api/pptos", {"monto": 150.00, "fecha": date.today().isoformat()})

    print("5/8 Recibiendo orden de restock")
    order = post(
        "/api/ordenes-restock",
        {
            "sucursalId": origin["id"],
            "pptoId": ppto["id"],
            "repuestos": [
                {"productoId": PRODUCT_ID, "cantidad": 10, "costoUnitario": 12.00}
            ],
            "estado": "RECIBIDA",
        },
    )

    print("6/8 Calculando GAP")
    gap = post("/api/gaps", {"ordenDeRestockId": order["id"]})

    print("7/8 Traspasando cuatro unidades")
    transfer = post(
        "/api/traspasos",
        {
            "origenId": origin["id"],
            "destinoId": destination["id"],
            "productoId": PRODUCT_ID,
            "cantidad": 4,
            "estado": "COMPLETADO",
        },
    )

    print("8/8 Consultando inventario final")
    inventory = request("GET", "/api/inventarios")
    case_inventory = [item for item in inventory if item["productoId"] == PRODUCT_ID]

    print("\nGAP calculado:")
    print(json.dumps(gap, indent=2, ensure_ascii=False))
    print("\nTraspaso:")
    print(json.dumps(transfer, indent=2, ensure_ascii=False))
    print(f"\nInventario del caso {RUN_ID}:")
    print(json.dumps(case_inventory, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    try:
        run_demo()
    except (RuntimeError, KeyError, ValueError) as error:
        print(f"Error: {error}", file=sys.stderr)
        sys.exit(1)
