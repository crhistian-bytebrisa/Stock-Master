package com.example.stockmaster.controller;

import com.example.stockmaster.dto.PPTORequest;
import com.example.stockmaster.dto.PPTOResponse;
import com.example.stockmaster.service.PPTOService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pptos")
public class PPTOController {
    private final PPTOService service;
    public PPTOController(PPTOService service) { this.service=service; }
    // POST /api/pptos - Crear un presupuesto.
    @PostMapping public ResponseEntity<PPTOResponse> crear(@Valid @RequestBody PPTORequest r) { var v=service.crear(r); return ResponseEntity.created(URI.create("/api/pptos/"+v.id())).body(v); }
    // GET /api/pptos - Listar todos los presupuestos.
    @GetMapping public List<PPTOResponse> listar() { return service.listar(); }
    // GET /api/pptos/{id} - Buscar un presupuesto por id.
    @GetMapping("/{id}") public PPTOResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    // PUT /api/pptos/{id} - Actualizar un presupuesto.
    @PutMapping("/{id}") public PPTOResponse actualizar(@PathVariable Long id,@Valid @RequestBody PPTORequest r) { return service.actualizar(id,r); }
    // DELETE /api/pptos/{id} - Eliminar un presupuesto.
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
