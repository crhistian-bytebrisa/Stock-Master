package com.example.stockmaster.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gaps_presupuesto")
public class GAP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_restock_id", nullable = false, unique = true)
    private OrdenDeRestock ordenDeRestock;

    private BigDecimal montoPresupuestado;
    private BigDecimal costoReal;
    private BigDecimal diferencia;
    private LocalDateTime fechaCalculo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public OrdenDeRestock getOrdenDeRestock() { return ordenDeRestock; }
    public void setOrdenDeRestock(OrdenDeRestock ordenDeRestock) { this.ordenDeRestock = ordenDeRestock; }
    public BigDecimal getMontoPresupuestado() { return montoPresupuestado; }
    public void setMontoPresupuestado(BigDecimal montoPresupuestado) { this.montoPresupuestado = montoPresupuestado; }
    public BigDecimal getCostoReal() { return costoReal; }
    public void setCostoReal(BigDecimal costoReal) { this.costoReal = costoReal; }
    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }
    public LocalDateTime getFechaCalculo() { return fechaCalculo; }
    public void setFechaCalculo(LocalDateTime fechaCalculo) { this.fechaCalculo = fechaCalculo; }
}
