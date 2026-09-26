package com.example.stockmaster.service;

import com.example.stockmaster.dto.PPTORequest;
import com.example.stockmaster.dto.PPTOResponse;
import com.example.stockmaster.exception.ResourceNotFoundException;
import com.example.stockmaster.mapper.PPTOMapper;
import com.example.stockmaster.model.PPTO;
import com.example.stockmaster.repository.PPTORepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PPTOService {
    private final PPTORepository repository;

    public PPTOService(PPTORepository repository) { this.repository = repository; }
    public PPTOResponse crear(PPTORequest request) { return PPTOMapper.toResponse(repository.save(aplicar(new PPTO(), request))); }
    @Transactional(readOnly = true)
    public List<PPTOResponse> listar() { return repository.findAll().stream().map(PPTOMapper::toResponse).toList(); }
    @Transactional(readOnly = true)
    public PPTOResponse buscar(Long id) { return PPTOMapper.toResponse(obtener(id)); }
    public PPTOResponse actualizar(Long id, PPTORequest request) { return PPTOMapper.toResponse(repository.save(aplicar(obtener(id), request))); }
    public void eliminar(Long id) { repository.delete(obtener(id)); }
    public PPTO obtener(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("PPTO", id)); }
    private PPTO aplicar(PPTO value, PPTORequest request) { value.setMonto(request.monto()); value.setFecha(request.fecha()); return value; }
}
