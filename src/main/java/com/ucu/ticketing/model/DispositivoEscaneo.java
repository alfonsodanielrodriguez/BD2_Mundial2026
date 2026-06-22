package com.ucu.ticketing.model;

import jakarta.persistence.*;

@Entity
@Table(name = "DISPOSITIVO_ESCANEO_AUTORIZADO")
public class DispositivoEscaneo {

    @Id
    @Column(name = "id_dispositivo", length = 100)
    private String idDispositivo;

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String v) { this.idDispositivo = v; }
}