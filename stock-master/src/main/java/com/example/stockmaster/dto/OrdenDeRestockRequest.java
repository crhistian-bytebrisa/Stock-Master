package com.example.stockmaster.dto;

import com.example.stockmaster.model.enums.EstadoRestock;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record OrdenDeRestockRequest(
        @NotNull Long sucursalId,
        @NotNull Long pptoId,
        @NotEmpty List<@Valid RepuestoRequest> repuestos,
        LocalDateTime fecha,
        EstadoRestock estado) {
}
