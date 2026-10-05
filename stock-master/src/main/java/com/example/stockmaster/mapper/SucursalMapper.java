package com.example.stockmaster.mapper;

import com.example.stockmaster.dto.SucursalResponse;
import com.example.stockmaster.model.Sucursal;

public final class SucursalMapper {
    private SucursalMapper() { }

    public static SucursalResponse toResponse(Sucursal value) {
        return new SucursalResponse(value.getId(), value.getCodigo(), value.getNombre(),
                value.getDireccion(), value.isActiva());
    }
}
