package com.example.stockmaster.dto;

public record InventarioResponse(
        Long id, Long sucursalId, String sucursalNombre,
        String productoId, String productoNombre, Integer cantidad) {
}
