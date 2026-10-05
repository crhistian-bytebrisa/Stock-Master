package com.example.stockmaster.mapper;

import com.example.stockmaster.dto.GAPResponse;
import com.example.stockmaster.model.GAP;

public final class GAPMapper {
    private GAPMapper() { }

    public static GAPResponse toResponse(GAP value) {
        return new GAPResponse(value.getId(), value.getOrdenDeRestock().getId(),
                value.getMontoPresupuestado(), value.getCostoReal(),
                value.getDiferencia(), value.getFechaCalculo());
    }
}
