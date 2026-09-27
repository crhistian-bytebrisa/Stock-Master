package com.example.stockmaster.service;

import com.example.stockmaster.dto.SucursalRequest;
import com.example.stockmaster.dto.SucursalResponse;
import com.example.stockmaster.exception.BusinessException;
import com.example.stockmaster.exception.ResourceNotFoundException;
import com.example.stockmaster.mapper.SucursalMapper;
import com.example.stockmaster.model.Sucursal;
import com.example.stockmaster.repository.SucursalRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SucursalService {
    private final SucursalRepository repository;

    public SucursalService(SucursalRepository repository) { this.repository = repository; }

    public SucursalResponse crear(SucursalRequest request) {
        if (repository.existsByCodigoIgnoreCase(request.codigo())) {
            throw new BusinessException("Ya existe una sucursal con el código " + request.codigo());
        }
        return SucursalMapper.toResponse(repository.save(aplicar(new Sucursal(), request)));
    }

    @Transactional(readOnly = true)
    public List<SucursalResponse> listar() {
        return repository.findAll().stream().map(SucursalMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SucursalResponse buscar(Long id) { return SucursalMapper.toResponse(obtener(id)); }

    public SucursalResponse actualizar(Long id, SucursalRequest request) {
        Sucursal value = obtener(id);
        if (!value.getCodigo().equalsIgnoreCase(request.codigo())
                && repository.existsByCodigoIgnoreCase(request.codigo())) {
            throw new BusinessException("Ya existe una sucursal con el código " + request.codigo());
        }
        return SucursalMapper.toResponse(repository.save(aplicar(value, request)));
    }

    public void eliminar(Long id) { repository.delete(obtener(id)); }

    public Sucursal obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sucursal", id));
    }

    private Sucursal aplicar(Sucursal value, SucursalRequest request) {
        value.setCodigo(request.codigo().trim());
        value.setNombre(request.nombre().trim());
        value.setDireccion(request.direccion());
        value.setActiva(request.activa() == null || request.activa());
        return value;
    }
}
