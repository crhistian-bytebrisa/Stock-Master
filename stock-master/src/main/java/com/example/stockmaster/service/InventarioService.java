package com.example.stockmaster.service;

import com.example.stockmaster.dto.InventarioRequest;
import com.example.stockmaster.dto.InventarioResponse;
import com.example.stockmaster.exception.BusinessException;
import com.example.stockmaster.exception.ResourceNotFoundException;
import com.example.stockmaster.mapper.InventarioMapper;
import com.example.stockmaster.model.Inventario;
import com.example.stockmaster.repository.InventarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InventarioService {
    private final InventarioRepository repository;
    private final SucursalService sucursalService;
    private final ProductosService productosService;

    public InventarioService(InventarioRepository repository, SucursalService sucursalService, ProductosService productosService) {
        this.repository = repository; this.sucursalService = sucursalService; this.productosService = productosService;
    }
    public InventarioResponse crear(InventarioRequest request) {
        if (repository.existsBySucursalIdAndProductoId(request.sucursalId(), request.productoId())) {
            throw new BusinessException("Ya existe inventario para esa sucursal y producto");
        }
        return InventarioMapper.toResponse(repository.save(aplicar(new Inventario(), request)));
    }
    @Transactional(readOnly = true)
    public List<InventarioResponse> listar() { return repository.findAll().stream().map(InventarioMapper::toResponse).toList(); }
    @Transactional(readOnly = true)
    public InventarioResponse buscar(Long id) { return InventarioMapper.toResponse(obtener(id)); }
    public InventarioResponse actualizar(Long id, InventarioRequest request) {
        Inventario current = obtener(id);
        repository.findBySucursalIdAndProductoId(request.sucursalId(), request.productoId())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new BusinessException("Ya existe inventario para esa sucursal y producto"); });
        return InventarioMapper.toResponse(repository.save(aplicar(current, request)));
    }
    public void eliminar(Long id) { repository.delete(obtener(id)); }
    public Inventario obtener(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Inventario", id)); }
    public Inventario obtener(Long sucursalId, String productoId) {
        return repository.findBySucursalIdAndProductoId(sucursalId, productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario sucursal/producto", sucursalId + "/" + productoId));
    }
    public void cambiarStock(Long sucursalId, String productoId, int delta) {
        Inventario inventario = obtener(sucursalId, productoId);
        int nuevaCantidad = inventario.getCantidad() + delta;
        if (nuevaCantidad < 0) throw new BusinessException("Stock insuficiente para el producto " + productoId);
        inventario.setCantidad(nuevaCantidad);
        repository.save(inventario);
    }
    public void ingresarStock(Long sucursalId, String productoId, int cantidad) {
        Inventario inventario = repository.findBySucursalIdAndProductoId(sucursalId, productoId)
                .orElseGet(() -> {
                    Inventario nuevo = new Inventario();
                    nuevo.setSucursal(sucursalService.obtener(sucursalId));
                    nuevo.setProducto(productosService.obtener(productoId));
                    nuevo.setCantidad(0);
                    return nuevo;
                });
        inventario.setCantidad(inventario.getCantidad() + cantidad);
        repository.save(inventario);
    }
    private Inventario aplicar(Inventario value, InventarioRequest request) {
        value.setSucursal(sucursalService.obtener(request.sucursalId()));
        value.setProducto(productosService.obtener(request.productoId()));
        value.setCantidad(request.cantidad());
        return value;
    }
}
