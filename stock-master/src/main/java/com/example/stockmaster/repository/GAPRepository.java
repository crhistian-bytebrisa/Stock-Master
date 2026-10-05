package com.example.stockmaster.repository;

import com.example.stockmaster.model.GAP;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GAPRepository extends JpaRepository<GAP, Long> {
    Optional<GAP> findByOrdenDeRestockId(Long ordenDeRestockId);
    boolean existsByOrdenDeRestockId(Long ordenDeRestockId);
}
