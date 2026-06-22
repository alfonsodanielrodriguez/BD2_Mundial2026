package com.ucu.ticketing.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "ENCUENTRO")
public class Encuentro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_encuentro")
    private Integer idEncuentro;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @ManyToOne
    @JoinColumn(name = "id_estadio", nullable = false)
    private Estadio estadio;

    @ManyToOne
    @JoinColumn(name = "pais_equipo_local", nullable = false)
    private Equipo equipoLocal;

    @ManyToOne
    @JoinColumn(name = "pais_equipo_visitante", nullable = false)
    private Equipo equipoVisitante;

    @ManyToOne
    @JoinColumn(name = "direccion_correo_administrador", nullable = false)
    private Administrador administrador;

    public Integer getIdEncuentro() { return idEncuentro; }
    public void setIdEncuentro(Integer v) { this.idEncuentro = v; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate v) { this.fecha = v; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime v) { this.hora = v; }

    public Estadio getEstadio() { return estadio; }
    public void setEstadio(Estadio v) { this.estadio = v; }

    public Equipo getEquipoLocal() { return equipoLocal; }
    public void setEquipoLocal(Equipo v) { this.equipoLocal = v; }

    public Equipo getEquipoVisitante() { return equipoVisitante; }
    public void setEquipoVisitante(Equipo v) { this.equipoVisitante = v; }

    public Administrador getAdministrador() { return administrador; }
    public void setAdministrador(Administrador v) { this.administrador = v; }
}