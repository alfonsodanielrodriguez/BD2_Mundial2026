package com.ucu.ticketing.model;

import jakarta.persistence.*;

@Entity
@Table(name = "FUNCIONARIO_DE_VALIDACION")
@PrimaryKeyJoinColumn(name = "direccion_correo_electronico")
public class FuncionarioValidacion extends Usuario {

    @Column(name = "numero_legajo", nullable = false, length = 50)
    private String numeroLegajo;

    public String getNumeroLegajo() { return numeroLegajo; }
    public void setNumeroLegajo(String v) { this.numeroLegajo = v; }
}