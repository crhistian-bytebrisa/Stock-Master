package com.example.stockmaster.dto;

import com.example.stockmaster.model.enums.EstadoTraspaso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record TraspasoRequest(
        @NotNull Long origenId,
        @NotNull Long destinoId,
        @NotBlank String productoId,
        @NotNull @Positive Integer cantidad,
        LocalDateTime fecha,
        EstadoTraspaso estado) {
}
