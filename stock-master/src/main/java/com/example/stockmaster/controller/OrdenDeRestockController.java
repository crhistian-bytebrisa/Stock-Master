package com.example.stockmaster.controller;

import com.example.stockmaster.dto.OrdenDeRestockRequest;
import com.example.stockmaster.dto.OrdenDeRestockResponse;
import com.example.stockmaster.service.OrdenDeRestockService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ordenes-restock")
public class OrdenDeRestockController {
    private final OrdenDeRestockService service;
    public OrdenDeRestockController(OrdenDeRestockService service) { this.service=service; }
    // POST /api/ordenes-restock - Crear una orden de restock.
    @PostMapping public ResponseEntity<OrdenDeRestockResponse> crear(@Valid @RequestBody OrdenDeRestockRequest r) { var v=service.crear(r); return ResponseEntity.created(URI.create("/api/ordenes-restock/"+v.id())).body(v); }
    // GET /api/ordenes-restock - Listar todas las órdenes de restock.
    @GetMapping public List<OrdenDeRestockResponse> listar() { return service.listar(); }
    // GET /api/ordenes-restock/{id} - Buscar una orden de restock por id.
    @GetMapping("/{id}") public OrdenDeRestockResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    // PUT /api/ordenes-restock/{id} - Actualizar una orden de restock.
    @PutMapping("/{id}") public OrdenDeRestockResponse actualizar(@PathVariable Long id,@Valid @RequestBody OrdenDeRestockRequest r) { return service.actualizar(id,r); }
    // DELETE /api/ordenes-restock/{id} - Eliminar una orden de restock.
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
