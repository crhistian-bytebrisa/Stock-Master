package com.example.stockmaster.controller;

import com.example.stockmaster.dto.InventarioRequest;
import com.example.stockmaster.dto.InventarioResponse;
import com.example.stockmaster.service.InventarioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventarios")
public class InventarioController {
    private final InventarioService service;
    public InventarioController(InventarioService service) { this.service=service; }
    // POST /api/inventarios - Crear un registro de inventario.
    @PostMapping public ResponseEntity<InventarioResponse> crear(@Valid @RequestBody InventarioRequest r) { var v=service.crear(r); return ResponseEntity.created(URI.create("/api/inventarios/"+v.id())).body(v); }
    // GET /api/inventarios - Listar todos los inventarios.
    @GetMapping public List<InventarioResponse> listar() { return service.listar(); }
    // GET /api/inventarios/{id} - Buscar un inventario por id.
    @GetMapping("/{id}") public InventarioResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    // PUT /api/inventarios/{id} - Actualizar un inventario.
    @PutMapping("/{id}") public InventarioResponse actualizar(@PathVariable Long id,@Valid @RequestBody InventarioRequest r) { return service.actualizar(id,r); }
    // DELETE /api/inventarios/{id} - Eliminar un inventario.
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
