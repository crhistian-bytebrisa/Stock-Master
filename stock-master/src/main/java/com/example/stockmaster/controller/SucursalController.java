package com.example.stockmaster.controller;

import com.example.stockmaster.dto.SucursalRequest;
import com.example.stockmaster.dto.SucursalResponse;
import com.example.stockmaster.service.SucursalService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sucursales")
public class SucursalController {
    private final SucursalService service;
    public SucursalController(SucursalService service) { this.service = service; }
    // POST /api/sucursales - Crear una sucursal.
    @PostMapping public ResponseEntity<SucursalResponse> crear(@Valid @RequestBody SucursalRequest r) { var v=service.crear(r); return ResponseEntity.created(URI.create("/api/sucursales/"+v.id())).body(v); }
    // GET /api/sucursales - Listar todas las sucursales.
    @GetMapping public List<SucursalResponse> listar() { return service.listar(); }
    // GET /api/sucursales/{id} - Buscar una sucursal por id.
    @GetMapping("/{id}") public SucursalResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    // PUT /api/sucursales/{id} - Actualizar una sucursal.
    @PutMapping("/{id}") public SucursalResponse actualizar(@PathVariable Long id,@Valid @RequestBody SucursalRequest r) { return service.actualizar(id,r); }
    // DELETE /api/sucursales/{id} - Eliminar una sucursal.
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
