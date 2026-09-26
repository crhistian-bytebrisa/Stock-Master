package com.example.stockmaster.controller;

import com.example.stockmaster.dto.RepuestoRequest;
import com.example.stockmaster.dto.RepuestoResponse;
import com.example.stockmaster.service.RepuestoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/repuestos")
public class RepuestoController {
    private final RepuestoService service;
    public RepuestoController(RepuestoService service) { this.service=service; }
    // POST /api/repuestos - Crear un repuesto.
    @PostMapping public ResponseEntity<RepuestoResponse> crear(@Valid @RequestBody RepuestoRequest r) { var v=service.crear(r); return ResponseEntity.created(URI.create("/api/repuestos/"+v.id())).body(v); }
    // GET /api/repuestos - Listar todos los repuestos.
    @GetMapping public List<RepuestoResponse> listar() { return service.listar(); }
    // GET /api/repuestos/{id} - Buscar un repuesto por id.
    @GetMapping("/{id}") public RepuestoResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    // PUT /api/repuestos/{id} - Actualizar un repuesto.
    @PutMapping("/{id}") public RepuestoResponse actualizar(@PathVariable Long id,@Valid @RequestBody RepuestoRequest r) { return service.actualizar(id,r); }
    // DELETE /api/repuestos/{id} - Eliminar un repuesto.
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
