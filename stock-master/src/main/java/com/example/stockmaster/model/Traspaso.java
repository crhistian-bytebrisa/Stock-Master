package com.example.stockmaster.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.stockmaster.model.enums.EstadoTraspaso;

@Entity
@Table(name = "traspasos")
public class Traspaso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origen_id", nullable = false)
    private Sucursal origen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destino_id", nullable = false)
    private Sucursal destino;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Productos producto;

    private Integer cantidad;

    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    private EstadoTraspaso estado = EstadoTraspaso.PENDIENTE;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Sucursal getOrigen() { return origen; }
    public void setOrigen(Sucursal origen) { this.origen = origen; }
    public Sucursal getDestino() { return destino; }
    public void setDestino(Sucursal destino) { this.destino = destino; }
    public Productos getProducto() { return producto; }
    public void setProducto(Productos producto) { this.producto = producto; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public EstadoTraspaso getEstado() { return estado; }
    public void setEstado(EstadoTraspaso estado) { this.estado = estado; }
}
