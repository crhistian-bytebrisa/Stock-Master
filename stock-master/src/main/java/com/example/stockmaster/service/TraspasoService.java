package com.example.stockmaster.service;

import com.example.stockmaster.dto.TraspasoRequest;
import com.example.stockmaster.dto.TraspasoResponse;
import com.example.stockmaster.exception.BusinessException;
import com.example.stockmaster.exception.ResourceNotFoundException;
import com.example.stockmaster.mapper.TraspasoMapper;
import com.example.stockmaster.model.Traspaso;
import com.example.stockmaster.model.enums.EstadoTraspaso;
import com.example.stockmaster.repository.TraspasoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TraspasoService {
    private final TraspasoRepository repository;
    private final SucursalService sucursalService;
    private final ProductosService productosService;
    private final InventarioService inventarioService;

    public TraspasoService(TraspasoRepository repository, SucursalService sucursalService,
            ProductosService productosService, InventarioService inventarioService) {
        this.repository = repository; this.sucursalService = sucursalService;
        this.productosService = productosService; this.inventarioService = inventarioService;
    }
    public TraspasoResponse crear(TraspasoRequest request) {
        Traspaso value = aplicar(new Traspaso(), request);
        if (value.getEstado() == EstadoTraspaso.COMPLETADO) moverStock(value);
        return TraspasoMapper.toResponse(repository.save(value));
    }
    @Transactional(readOnly = true)
    public List<TraspasoResponse> listar() { return repository.findAll().stream().map(TraspasoMapper::toResponse).toList(); }
    @Transactional(readOnly = true)
    public TraspasoResponse buscar(Long id) { return TraspasoMapper.toResponse(obtener(id)); }
    public TraspasoResponse actualizar(Long id, TraspasoRequest request) {
        Traspaso value = obtener(id);
        EstadoTraspaso anterior = value.getEstado();
        if (anterior == EstadoTraspaso.COMPLETADO) throw new BusinessException("Un traspaso completado no puede modificarse");
        aplicar(value, request);
        if (anterior != EstadoTraspaso.COMPLETADO && value.getEstado() == EstadoTraspaso.COMPLETADO) moverStock(value);
        return TraspasoMapper.toResponse(repository.save(value));
    }
    public void eliminar(Long id) {
        Traspaso value = obtener(id);
        if (value.getEstado() == EstadoTraspaso.COMPLETADO) throw new BusinessException("Un traspaso completado no puede eliminarse");
        repository.delete(value);
    }
    public Traspaso obtener(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Traspaso", id)); }
    private Traspaso aplicar(Traspaso value, TraspasoRequest request) {
        if (request.origenId().equals(request.destinoId())) throw new BusinessException("Origen y destino deben ser diferentes");
        value.setOrigen(sucursalService.obtener(request.origenId()));
        value.setDestino(sucursalService.obtener(request.destinoId()));
        value.setProducto(productosService.obtener(request.productoId()));
        value.setCantidad(request.cantidad());
        value.setFecha(request.fecha() == null ? LocalDateTime.now() : request.fecha());
        value.setEstado(request.estado() == null ? EstadoTraspaso.PENDIENTE : request.estado());
        return value;
    }
    private void moverStock(Traspaso value) {
        inventarioService.cambiarStock(value.getOrigen().getId(), value.getProducto().getId(), -value.getCantidad());
        inventarioService.ingresarStock(value.getDestino().getId(), value.getProducto().getId(), value.getCantidad());
    }
}
