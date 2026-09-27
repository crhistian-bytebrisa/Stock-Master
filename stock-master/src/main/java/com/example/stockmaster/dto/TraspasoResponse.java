package com.example.stockmaster.dto;

import com.example.stockmaster.model.enums.EstadoTraspaso;
import java.time.LocalDateTime;

public record TraspasoResponse(
        Long id, Long origenId, String origenNombre, Long destinoId, String destinoNombre,
        String productoId, String productoNombre, Integer cantidad,
        LocalDateTime fecha, EstadoTraspaso estado) {
}
