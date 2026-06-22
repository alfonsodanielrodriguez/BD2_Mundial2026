package com.ucu.ticketing.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "ADMINISTRADOR_POR_PAIS_SEDE")
@PrimaryKeyJoinColumn(name = "direccion_correo_electronico")
public class Administrador extends Usuario {

    @Column(name = "pais_que_administra", nullable = false, length = 100)
    private String paisQueAdministra;

    @Column(name = "fecha_asignacion_cargo", nullable = false)
    private LocalDate fechaAsignacionCargo;

    public String getPaisQueAdministra() { return paisQueAdministra; }
    public void setPaisQueAdministra(String v) { this.paisQueAdministra = v; }

    public LocalDate getFechaAsignacionCargo() { return fechaAsignacionCargo; }
    public void setFechaAsignacionCargo(LocalDate v) { this.fechaAsignacionCargo = v; }
}