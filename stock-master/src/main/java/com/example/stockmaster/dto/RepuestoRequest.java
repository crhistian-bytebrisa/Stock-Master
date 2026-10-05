package com.example.stockmaster.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record RepuestoRequest(
        @NotBlank String productoId,
        @NotNull @Positive Integer cantidad,
        @NotNull @DecimalMin("0.0") BigDecimal costoUnitario) {
}
