package com.example.stockmaster.dto;

import com.example.stockmaster.model.enums.EstadoRestock;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenDeRestockResponse(
        Long id, Long sucursalId, String sucursalNombre, Long pptoId,
        List<RepuestoResponse> repuestos, BigDecimal costoTotal,
        LocalDateTime fecha, EstadoRestock estado) {
}
