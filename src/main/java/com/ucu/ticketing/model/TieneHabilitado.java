package com.ucu.ticketing.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "TIENE_HABILITADO")
@IdClass(TieneHabilitado.TieneHabilitadoId.class)
public class TieneHabilitado {

    @Id
    @Column(name = "id_encuentro")
    private Integer idEncuentro;

    @Id
    @Column(name = "letra", length = 1, columnDefinition = "CHAR(1)")
    private String letra;

    @Id
    @Column(name = "id_estadio")
    private Integer idEstadio;

    public Integer getIdEncuentro() { return idEncuentro; }
    public void setIdEncuentro(Integer v) { this.idEncuentro = v; }

    public String getLetra() { return letra; }
    public void setLetra(String v) { this.letra = v; }

    public Integer getIdEstadio() { return idEstadio; }
    public void setIdEstadio(Integer v) { this.idEstadio = v; }

    public static class TieneHabilitadoId implements Serializable {
        private Integer idEncuentro;
        private String letra;
        private Integer idEstadio;

        public void setIdEncuentro(Integer v) { this.idEncuentro = v; }
        public void setLetra(String v) { this.letra = v; }
        public void setIdEstadio(Integer v) { this.idEstadio = v; }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TieneHabilitadoId t)) return false;
            return idEncuentro.equals(t.idEncuentro) && letra.equals(t.letra) && idEstadio.equals(t.idEstadio);
        }

        @Override
        public int hashCode() {
            return 31 * idEncuentro.hashCode() + letra.hashCode() + idEstadio.hashCode();
        }
    }
}