package com.example.stockmaster.controller;

import com.example.stockmaster.dto.GAPRequest;
import com.example.stockmaster.dto.GAPResponse;
import com.example.stockmaster.service.GAPService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gaps")
public class GAPController {
    private final GAPService service;
    public GAPController(GAPService service) { this.service=service; }
    // POST /api/gaps - Calcular y crear un GAP presupuestario.
    @PostMapping public ResponseEntity<GAPResponse> crear(@Valid @RequestBody GAPRequest r) { var v=service.crear(r); return ResponseEntity.created(URI.create("/api/gaps/"+v.id())).body(v); }
    // GET /api/gaps - Listar todos los GAP presupuestarios.
    @GetMapping public List<GAPResponse> listar() { return service.listar(); }
    // GET /api/gaps/{id} - Buscar un GAP por id.
    @GetMapping("/{id}") public GAPResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    // PUT /api/gaps/{id} - Recalcular un GAP.
    @PutMapping("/{id}") public GAPResponse actualizar(@PathVariable Long id,@Valid @RequestBody GAPRequest r) { return service.actualizar(id,r); }
    // DELETE /api/gaps/{id} - Eliminar un GAP.
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
