package com.example.stockmaster.dto;

import jakarta.validation.constraints.NotNull;

public record GAPRequest(@NotNull Long ordenDeRestockId) {
}
