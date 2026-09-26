package com.example.stockmaster.dto;

import java.math.BigDecimal;

public record ProductosResponse(
        String id, String nombre, String descripcion,
        BigDecimal precioCompra, BigDecimal precioVenta) {
}
