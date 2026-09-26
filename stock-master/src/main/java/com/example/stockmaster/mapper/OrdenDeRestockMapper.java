package com.example.stockmaster.mapper;

import com.example.stockmaster.dto.OrdenDeRestockResponse;
import com.example.stockmaster.model.OrdenDeRestock;

public final class OrdenDeRestockMapper {
    private OrdenDeRestockMapper() { }

    public static OrdenDeRestockResponse toResponse(OrdenDeRestock value) {
        return new OrdenDeRestockResponse(value.getId(), value.getSucursal().getId(),
                value.getSucursal().getNombre(), value.getPpto().getId(),
                value.getRepuestos().stream().map(RepuestoMapper::toResponse).toList(),
                value.getCostoTotal(), value.getFecha(), value.getEstado());
    }
}
