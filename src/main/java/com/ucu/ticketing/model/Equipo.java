package com.ucu.ticketing.model;

import jakarta.persistence.*;

@Entity
@Table(name = "EQUIPO")
public class Equipo {

    @Id
    @Column(name = "pais", length = 100)
    private String pais;

    public String getPais() { return pais; }
    public void setPais(String v) { this.pais = v; }
}