package com.example.stockmaster.repository;

import com.example.stockmaster.model.Repuesto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepuestoRepository extends JpaRepository<Repuesto, Long> {
}
