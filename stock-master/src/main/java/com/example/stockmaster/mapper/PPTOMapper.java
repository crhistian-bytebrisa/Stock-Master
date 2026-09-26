package com.example.stockmaster.mapper;

import com.example.stockmaster.dto.PPTOResponse;
import com.example.stockmaster.model.PPTO;

public final class PPTOMapper {
    private PPTOMapper() { }

    public static PPTOResponse toResponse(PPTO value) {
        return new PPTOResponse(value.getId(), value.getMonto(), value.getFecha());
    }
}
