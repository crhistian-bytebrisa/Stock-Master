package com.example.stockmaster.mapper;

import com.example.stockmaster.dto.TraspasoResponse;
import com.example.stockmaster.model.Traspaso;

public final class TraspasoMapper {
    private TraspasoMapper() { }

    public static TraspasoResponse toResponse(Traspaso value) {
        return new TraspasoResponse(value.getId(), value.getOrigen().getId(),
                value.getOrigen().getNombre(), value.getDestino().getId(),
                value.getDestino().getNombre(), value.getProducto().getId(),
                value.getProducto().getNombre(), value.getCantidad(), value.getFecha(), value.getEstado());
    }
}
