package com.example.stockmaster.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PPTORequest(
        @NotNull @DecimalMin("0.0") BigDecimal monto,
        @NotNull LocalDate fecha) {
}
