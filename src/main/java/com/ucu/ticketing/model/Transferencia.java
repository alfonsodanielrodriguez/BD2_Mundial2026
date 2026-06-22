package com.ucu.ticketing.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "TRANSFERENCIA")
public class Transferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transferencia")
    private Integer idTransferencia;

    @ManyToOne
    @JoinColumn(name = "usuario_emisor", nullable = false)
    private UsuarioGeneral emisor;

    @ManyToOne
    @JoinColumn(name = "usuario_receptor", nullable = false)
    private UsuarioGeneral receptor;

    @ManyToOne
    @JoinColumn(name = "id_entrada", nullable = false)
    private Entrada entrada;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado = Estado.pendiente;

    public enum Estado { pendiente, aceptada, rechazada }

    public Integer getIdTransferencia() { return idTransferencia; }
    public void setIdTransferencia(Integer v) { this.idTransferencia = v; }

    public UsuarioGeneral getEmisor() { return emisor; }
    public void setEmisor(UsuarioGeneral v) { this.emisor = v; }

    public UsuarioGeneral getReceptor() { return receptor; }
    public void setReceptor(UsuarioGeneral v) { this.receptor = v; }

    public Entrada getEntrada() { return entrada; }
    public void setEntrada(Entrada v) { this.entrada = v; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate v) { this.fecha = v; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime v) { this.hora = v; }

    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime v) { this.fechaRespuesta = v; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado v) { this.estado = v; }
}