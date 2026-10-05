package com.example.stockmaster.service;

import com.example.stockmaster.dto.ProductosRequest;
import com.example.stockmaster.dto.ProductosResponse;
import com.example.stockmaster.exception.ResourceNotFoundException;
import com.example.stockmaster.mapper.ProductosMapper;
import com.example.stockmaster.model.Productos;
import com.example.stockmaster.repository.ProductosRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductosService {
    private final ProductosRepository repository;

    public ProductosService(ProductosRepository repository) { this.repository = repository; }

    public ProductosResponse crear(ProductosRequest request) {
        return ProductosMapper.toResponse(repository.save(aplicar(new Productos(), request, true)));
    }

    @Transactional(readOnly = true)
    public List<ProductosResponse> listar() {
        return repository.findAll().stream().map(ProductosMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProductosResponse buscar(String id) { return ProductosMapper.toResponse(obtener(id)); }

    public ProductosResponse actualizar(String id, ProductosRequest request) {
        return ProductosMapper.toResponse(repository.save(aplicar(obtener(id), request, false)));
    }

    public void eliminar(String id) { repository.delete(obtener(id)); }

    public Productos obtener(String id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }

    private Productos aplicar(Productos value, ProductosRequest request, boolean nuevo) {
        if (nuevo) value.setId(java.util.UUID.randomUUID().toString());
        value.setNombre(request.nombre().trim());
        value.setDescripcion(request.descripcion());
        value.setPrecioCompra(request.precioCompra());
        value.setPrecioVenta(request.precioVenta());
        return value;
    }
}
