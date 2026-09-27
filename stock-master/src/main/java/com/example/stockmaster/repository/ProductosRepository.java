package com.example.stockmaster.repository;

import com.example.stockmaster.model.Productos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductosRepository extends JpaRepository<Productos, String> {
}
