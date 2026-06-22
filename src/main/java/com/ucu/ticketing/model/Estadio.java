package com.ucu.ticketing.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ESTADIO")
public class Estadio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estadio")
    private Integer idEstadio;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "direccion", nullable = false, length = 255)
    private String direccion;

    @Column(name = "pais", nullable = false, length = 100)
    private String pais;

    @Column(name = "aforo", nullable = false)
    private Integer aforo;

    public Integer getIdEstadio() { return idEstadio; }
    public void setIdEstadio(Integer v) { this.idEstadio = v; }

    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String v) { this.direccion = v; }

    public String getPais() { return pais; }
    public void setPais(String v) { this.pais = v; }

    public Integer getAforo() { return aforo; }
    public void setAforo(Integer v) { this.aforo = v; }
}