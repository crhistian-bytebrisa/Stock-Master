package com.example.stockmaster.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.example.stockmaster.model.enums.EstadoRestock;

@Entity
@Table(name = "ordenes_restock")
public class OrdenDeRestock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ppto_id", nullable = false, unique = true)
    private PPTO ppto;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "orden_restock_id")
    private List<Repuesto> repuestos = new java.util.ArrayList<>();

    private BigDecimal costoTotal;

    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    private EstadoRestock estado = EstadoRestock.PENDIENTE;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
    public PPTO getPpto() { return ppto; }
    public void setPpto(PPTO ppto) { this.ppto = ppto; }
    public List<Repuesto> getRepuestos() { return repuestos; }
    public void setRepuestos(List<Repuesto> repuestos) { this.repuestos = repuestos; }
    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public EstadoRestock getEstado() { return estado; }
    public void setEstado(EstadoRestock estado) { this.estado = estado; }
}
