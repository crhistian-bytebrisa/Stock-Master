package com.example.stockmaster.mapper;

import com.example.stockmaster.dto.InventarioResponse;
import com.example.stockmaster.model.Inventario;

public final class InventarioMapper {
    private InventarioMapper() { }

    public static InventarioResponse toResponse(Inventario value) {
        return new InventarioResponse(value.getId(), value.getSucursal().getId(),
                value.getSucursal().getNombre(), value.getProducto().getId(),
                value.getProducto().getNombre(), value.getCantidad());
    }
}
