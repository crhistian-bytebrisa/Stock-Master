package com.example.stockmaster.dto;

import java.math.BigDecimal;

public record RepuestoResponse(
        Long id, String productoId, String productoNombre, Integer cantidad,
        BigDecimal costoUnitario, BigDecimal costoTotal) {
}
