package com.example.stockmaster.service;

import com.example.stockmaster.dto.GAPRequest;
import com.example.stockmaster.dto.GAPResponse;
import com.example.stockmaster.exception.BusinessException;
import com.example.stockmaster.exception.ResourceNotFoundException;
import com.example.stockmaster.mapper.GAPMapper;
import com.example.stockmaster.model.GAP;
import com.example.stockmaster.model.OrdenDeRestock;
import com.example.stockmaster.model.enums.EstadoRestock;
import com.example.stockmaster.repository.GAPRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GAPService {
    private final GAPRepository repository;
    private final OrdenDeRestockService ordenService;

    public GAPService(GAPRepository repository, OrdenDeRestockService ordenService) {
        this.repository = repository; this.ordenService = ordenService;
    }
    public GAPResponse crear(GAPRequest request) {
        if (repository.existsByOrdenDeRestockId(request.ordenDeRestockId())) {
            throw new BusinessException("La orden ya tiene un GAP calculado");
        }
        return GAPMapper.toResponse(repository.save(calcular(new GAP(), request.ordenDeRestockId())));
    }
    @Transactional(readOnly = true)
    public List<GAPResponse> listar() { return repository.findAll().stream().map(GAPMapper::toResponse).toList(); }
    @Transactional(readOnly = true)
    public GAPResponse buscar(Long id) { return GAPMapper.toResponse(obtener(id)); }
    public GAPResponse actualizar(Long id, GAPRequest request) {
        repository.findByOrdenDeRestockId(request.ordenDeRestockId())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> { throw new BusinessException("La orden ya tiene un GAP calculado"); });
        return GAPMapper.toResponse(repository.save(calcular(obtener(id), request.ordenDeRestockId())));
    }
    public void eliminar(Long id) { repository.delete(obtener(id)); }
    private GAP obtener(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("GAP", id)); }
    private GAP calcular(GAP value, Long ordenId) {
        OrdenDeRestock orden = ordenService.obtener(ordenId);
        if (orden.getEstado() != EstadoRestock.RECIBIDA) {
            throw new BusinessException("El GAP solo se calcula después de recibir el restock");
        }
        value.setOrdenDeRestock(orden);
        value.setMontoPresupuestado(orden.getPpto().getMonto());
        value.setCostoReal(orden.getCostoTotal());
        value.setDiferencia(orden.getPpto().getMonto().subtract(orden.getCostoTotal()));
        value.setFechaCalculo(LocalDateTime.now());
        return value;
    }
}
