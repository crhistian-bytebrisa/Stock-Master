package com.example.stockmaster.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductosRequest(
        @NotBlank @Size(max = 60) String id,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 500) String descripcion,
        @NotNull @DecimalMin("0.0") BigDecimal precioCompra,
        @NotNull @DecimalMin("0.0") BigDecimal precioVenta) {
}
