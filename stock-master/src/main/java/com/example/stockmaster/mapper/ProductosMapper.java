package com.example.stockmaster.mapper;

import com.example.stockmaster.dto.ProductosResponse;
import com.example.stockmaster.model.Productos;

public final class ProductosMapper {
    private ProductosMapper() { }

    public static ProductosResponse toResponse(Productos value) {
        return new ProductosResponse(value.getId(), value.getNombre(), value.getDescripcion(),
                value.getPrecioCompra(), value.getPrecioVenta());
    }
}
