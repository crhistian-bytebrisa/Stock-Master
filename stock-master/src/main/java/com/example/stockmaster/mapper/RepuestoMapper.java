package com.example.stockmaster.mapper;

import com.example.stockmaster.dto.RepuestoResponse;
import com.example.stockmaster.model.Repuesto;

public final class RepuestoMapper {
    private RepuestoMapper() { }

    public static RepuestoResponse toResponse(Repuesto value) {
        return new RepuestoResponse(value.getId(), value.getProducto().getId(),
                value.getProducto().getNombre(), value.getCantidad(),
                value.getCostoUnitario(), value.getCostoTotal());
    }
}
