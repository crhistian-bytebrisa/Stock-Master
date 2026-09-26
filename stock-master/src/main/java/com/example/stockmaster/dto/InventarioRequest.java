package com.example.stockmaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InventarioRequest(
        @NotNull Long sucursalId,
        @NotBlank String productoId,
        @NotNull @PositiveOrZero Integer cantidad) {
}
