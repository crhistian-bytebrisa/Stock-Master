package com.example.stockmaster.service;

import com.example.stockmaster.dto.RepuestoRequest;
import com.example.stockmaster.dto.RepuestoResponse;
import com.example.stockmaster.exception.ResourceNotFoundException;
import com.example.stockmaster.mapper.RepuestoMapper;
import com.example.stockmaster.model.Repuesto;
import com.example.stockmaster.repository.RepuestoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RepuestoService {
    private final RepuestoRepository repository;
    private final ProductosService productosService;

    public RepuestoService(RepuestoRepository repository, ProductosService productosService) {
        this.repository = repository; this.productosService = productosService;
    }
    public RepuestoResponse crear(RepuestoRequest request) { return RepuestoMapper.toResponse(repository.save(construir(new Repuesto(), request))); }
    @Transactional(readOnly = true)
    public List<RepuestoResponse> listar() { return repository.findAll().stream().map(RepuestoMapper::toResponse).toList(); }
    @Transactional(readOnly = true)
    public RepuestoResponse buscar(Long id) { return RepuestoMapper.toResponse(obtener(id)); }
    public RepuestoResponse actualizar(Long id, RepuestoRequest request) { return RepuestoMapper.toResponse(repository.save(construir(obtener(id), request))); }
    public void eliminar(Long id) { repository.delete(obtener(id)); }
    public Repuesto obtener(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Repuesto", id)); }
    public Repuesto construir(Repuesto value, RepuestoRequest request) {
        value.setProducto(productosService.obtener(request.productoId()));
        value.setCantidad(request.cantidad());
        value.setCostoUnitario(request.costoUnitario());
        value.setCostoTotal(request.costoUnitario().multiply(java.math.BigDecimal.valueOf(request.cantidad())));
        return value;
    }
}
