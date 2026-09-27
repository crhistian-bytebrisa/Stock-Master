package com.example.stockmaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SucursalRequest(
        @NotBlank @Size(max = 30) String codigo,
        @NotBlank @Size(max = 120) String nombre,
        @Size(max = 250) String direccion,
        Boolean activa) {
}
