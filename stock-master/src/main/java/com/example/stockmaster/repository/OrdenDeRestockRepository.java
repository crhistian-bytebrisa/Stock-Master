package com.example.stockmaster.repository;

import com.example.stockmaster.model.OrdenDeRestock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenDeRestockRepository extends JpaRepository<OrdenDeRestock, Long> {
    boolean existsByPptoId(Long pptoId);
}
