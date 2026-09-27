package com.example.stockmaster.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record GAPResponse(
        Long id, Long ordenDeRestockId, BigDecimal montoPresupuestado,
        BigDecimal costoReal, BigDecimal diferencia, LocalDateTime fechaCalculo) {
}
