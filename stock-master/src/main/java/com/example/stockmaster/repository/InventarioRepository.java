package com.example.stockmaster.repository;

import com.example.stockmaster.model.Inventario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    Optional<Inventario> findBySucursalIdAndProductoId(Long sucursalId, String productoId);
    boolean existsBySucursalIdAndProductoId(Long sucursalId, String productoId);
}
