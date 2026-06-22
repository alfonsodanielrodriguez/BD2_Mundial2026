package com.ucu.ticketing.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USUARIO_GENERAL")
@PrimaryKeyJoinColumn(name = "direccion_correo_electronico")
public class UsuarioGeneral extends Usuario {

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_verificacion_identidad", nullable = false)
    private EstadoVerificacion estadoVerificacionIdentidad = EstadoVerificacion.pendiente;

    @Column(name = "fecha_registro_en_sistema", nullable = false)
    private LocalDateTime fechaRegistroEnSistema = LocalDateTime.now();

    public enum EstadoVerificacion { pendiente, verificado, rechazado }

    public EstadoVerificacion getEstadoVerificacionIdentidad() { return estadoVerificacionIdentidad; }
    public void setEstadoVerificacionIdentidad(EstadoVerificacion v) { this.estadoVerificacionIdentidad = v; }

    public LocalDateTime getFechaRegistroEnSistema() { return fechaRegistroEnSistema; }
    public void setFechaRegistroEnSistema(LocalDateTime v) { this.fechaRegistroEnSistema = v; }
}