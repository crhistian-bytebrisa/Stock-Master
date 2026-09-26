package com.example.stockmaster.service;

import com.example.stockmaster.dto.OrdenDeRestockRequest;
import com.example.stockmaster.dto.OrdenDeRestockResponse;
import com.example.stockmaster.exception.BusinessException;
import com.example.stockmaster.exception.ResourceNotFoundException;
import com.example.stockmaster.mapper.OrdenDeRestockMapper;
import com.example.stockmaster.model.OrdenDeRestock;
import com.example.stockmaster.model.Repuesto;
import com.example.stockmaster.model.enums.EstadoRestock;
import com.example.stockmaster.repository.OrdenDeRestockRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrdenDeRestockService {
    private final OrdenDeRestockRepository repository;
    private final SucursalService sucursalService;
    private final PPTOService pptoService;
    private final RepuestoService repuestoService;
    private final InventarioService inventarioService;

    public OrdenDeRestockService(OrdenDeRestockRepository repository, SucursalService sucursalService,
            PPTOService pptoService, RepuestoService repuestoService, InventarioService inventarioService) {
        this.repository = repository; this.sucursalService = sucursalService; this.pptoService = pptoService;
        this.repuestoService = repuestoService; this.inventarioService = inventarioService;
    }

    public OrdenDeRestockResponse crear(OrdenDeRestockRequest request) {
        if (repository.existsByPptoId(request.pptoId())) throw new BusinessException("El PPTO ya está asociado a otra orden");
        OrdenDeRestock orden = aplicar(new OrdenDeRestock(), request);
        OrdenDeRestock saved = repository.save(orden);
        if (saved.getEstado() == EstadoRestock.RECIBIDA) ingresarStock(saved);
        return OrdenDeRestockMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrdenDeRestockResponse> listar() { return repository.findAll().stream().map(OrdenDeRestockMapper::toResponse).toList(); }
    @Transactional(readOnly = true)
    public OrdenDeRestockResponse buscar(Long id) { return OrdenDeRestockMapper.toResponse(obtener(id)); }

    public OrdenDeRestockResponse actualizar(Long id, OrdenDeRestockRequest request) {
        OrdenDeRestock orden = obtener(id);
        EstadoRestock anterior = orden.getEstado();
        if (anterior == EstadoRestock.RECIBIDA) throw new BusinessException("Una orden recibida no puede modificarse");
        repository.findAll().stream().filter(other -> !other.getId().equals(id))
                .filter(other -> other.getPpto().getId().equals(request.pptoId())).findAny()
                .ifPresent(other -> { throw new BusinessException("El PPTO ya está asociado a otra orden"); });
        aplicar(orden, request);
        OrdenDeRestock saved = repository.save(orden);
        if (anterior != EstadoRestock.RECIBIDA && saved.getEstado() == EstadoRestock.RECIBIDA) ingresarStock(saved);
        return OrdenDeRestockMapper.toResponse(saved);
    }

    public void eliminar(Long id) {
        OrdenDeRestock orden = obtener(id);
        if (orden.getEstado() == EstadoRestock.RECIBIDA) throw new BusinessException("Una orden recibida no puede eliminarse");
        repository.delete(orden);
    }

    public OrdenDeRestock obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Orden de restock", id));
    }

    private OrdenDeRestock aplicar(OrdenDeRestock value, OrdenDeRestockRequest request) {
        value.setSucursal(sucursalService.obtener(request.sucursalId()));
        value.setPpto(pptoService.obtener(request.pptoId()));
        List<Repuesto> repuestos = request.repuestos().stream()
                .map(item -> repuestoService.construir(new Repuesto(), item)).toList();
        value.getRepuestos().clear();
        value.getRepuestos().addAll(repuestos);
        value.setCostoTotal(repuestos.stream().map(Repuesto::getCostoTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        value.setFecha(request.fecha() == null ? LocalDateTime.now() : request.fecha());
        value.setEstado(request.estado() == null ? EstadoRestock.PENDIENTE : request.estado());
        return value;
    }

    private void ingresarStock(OrdenDeRestock orden) {
        for (Repuesto item : orden.getRepuestos()) {
            inventarioService.ingresarStock(orden.getSucursal().getId(), item.getProducto().getId(), item.getCantidad());
        }
    }
}
