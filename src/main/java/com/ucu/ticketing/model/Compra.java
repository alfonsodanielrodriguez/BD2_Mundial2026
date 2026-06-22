package com.ucu.ticketing.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "COMPRA")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Integer idCompra;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado = Estado.pendiente;

    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "comision_aplicada", nullable = false, precision = 5, scale = 2)
    private BigDecimal comisionAplicada = new BigDecimal("5.00");

    @ManyToOne
    @JoinColumn(name = "direccion_correo_usuario", nullable = false)
    private UsuarioGeneral usuario;

    public enum Estado { pendiente, confirmada, paga }

    public Integer getIdCompra() { return idCompra; }
    public void setIdCompra(Integer v) { this.idCompra = v; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate v) { this.fecha = v; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime v) { this.hora = v; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado v) { this.estado = v; }

    public BigDecimal getMontoTotal() { return montoTotal; }
    public void setMontoTotal(BigDecimal v) { this.montoTotal = v; }

    public BigDecimal getComisionAplicada() { return comisionAplicada; }
    public void setComisionAplicada(BigDecimal v) { this.comisionAplicada = v; }

    public UsuarioGeneral getUsuario() { return usuario; }
    public void setUsuario(UsuarioGeneral v) { this.usuario = v; }
}