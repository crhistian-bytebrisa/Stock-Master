package com.example.stockmaster.controller;

import com.example.stockmaster.dto.ProductosRequest;
import com.example.stockmaster.dto.ProductosResponse;
import com.example.stockmaster.service.ProductosService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productos")
public class ProductosController {
    private final ProductosService service;
    public ProductosController(ProductosService service) { this.service = service; }
    // POST /api/productos - Crear un producto.
    @PostMapping public ResponseEntity<ProductosResponse> crear(@Valid @RequestBody ProductosRequest r) { var v=service.crear(r); return ResponseEntity.created(URI.create("/api/productos/"+v.id())).body(v); }
    // GET /api/productos - Listar todos los productos.
    @GetMapping public List<ProductosResponse> listar() { return service.listar(); }
    // GET /api/productos/{id} - Buscar un producto por id.
    @GetMapping("/{id}") public ProductosResponse buscar(@PathVariable String id) { return service.buscar(id); }
    // PUT /api/productos/{id} - Actualizar un producto.
    @PutMapping("/{id}") public ProductosResponse actualizar(@PathVariable String id,@Valid @RequestBody ProductosRequest r) { return service.actualizar(id,r); }
    // DELETE /api/productos/{id} - Eliminar un producto.
    @DeleteMapping("/{id}") public ResponseEntity<Void> eliminar(@PathVariable String id) { service.eliminar(id); return ResponseEntity.noContent().build(); }
}
