package com.example.stockmaster.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PPTOResponse(Long id, BigDecimal monto, LocalDate fecha) {
}
