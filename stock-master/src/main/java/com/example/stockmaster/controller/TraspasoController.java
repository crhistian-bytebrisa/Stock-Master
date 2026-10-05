package com.example.stockmaster.controller;

import com.example.stockmaster.dto.TraspasoRequest;
import com.example.stockmaster.dto.TraspasoResponse;
import com.example.stockmaster.service.TraspasoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/traspasos")
public class TraspasoController {
    private final TraspasoService service;
    public TraspasoController(TraspasoService service) { this.service=service; }
    // POST /api/traspasos - Crear un traspaso.
    @PostMapping public ResponseEntity<TraspasoResponse> crear(@Valid @RequestBody TraspasoRequest r) { var v=service.crear(r); return ResponseEntity.created(URI.create("/api/traspasos/"+v.id())).body(v); }
    // GET /api/traspasos - Listar todos los traspasos.
    @GetMapping public List<TraspasoResponse> listar() { return service.listar(); }
    // GET /api/traspasos/{id} - Buscar un traspaso por id.
    @GetMapping("/{id}") public TraspasoResponse buscar(@PathVariable Long id) { return service.buscar(id); }
    // PUT /api/traspasos/{id} - Actualizar un traspaso.
    @PutMapping("/{id}") public TraspasoResponse actualizar(@PathVariable Long id,@Valid @RequestBody TraspasoRequest r) { return service.actualizar(id,r); }
    // DELETE /api/traspasos/{id} - Eliminar un traspaso.
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable Long id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
