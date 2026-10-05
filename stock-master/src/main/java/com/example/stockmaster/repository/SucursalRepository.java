package com.example.stockmaster.repository;

import com.example.stockmaster.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
}
