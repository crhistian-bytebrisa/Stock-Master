package com.example.stockmaster.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String recurso, Object id) {
        super(recurso + " no encontrado: " + id);
    }
}
