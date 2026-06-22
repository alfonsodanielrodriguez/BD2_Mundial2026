package com.ucu.ticketing.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "SECTOR")
@IdClass(Sector.SectorId.class)
public class Sector {

    @Id
    @Column(name = "letra", length = 1)
    private String letra;

    @Id
    @Column(name = "id_estadio")
    private Integer idEstadio;

    @ManyToOne
    @JoinColumn(name = "id_estadio", insertable = false, updatable = false)
    private Estadio estadio;

    @Column(name = "aforo", nullable = false)
    private Integer aforo;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    public String getLetra() { return letra; }
    public void setLetra(String v) { this.letra = v; }

    public Integer getIdEstadio() { return idEstadio; }
    public void setIdEstadio(Integer v) { this.idEstadio = v; }

    public Estadio getEstadio() { return estadio; }
    public void setEstadio(Estadio v) { this.estadio = v; }

    public Integer getAforo() { return aforo; }
    public void setAforo(Integer v) { this.aforo = v; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal v) { this.precio = v; }

    // Clave compuesta
    public static class SectorId implements Serializable {
        private String letra;
        private Integer idEstadio;

        public SectorId() {}
        public SectorId(String letra, Integer idEstadio) {
            this.letra = letra;
            this.idEstadio = idEstadio;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SectorId s)) return false;
            return letra.equals(s.letra) && idEstadio.equals(s.idEstadio);
        }

        @Override
        public int hashCode() {
            return 31 * letra.hashCode() + idEstadio.hashCode();
        }
    }
}