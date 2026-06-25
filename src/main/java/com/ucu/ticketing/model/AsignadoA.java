package com.ucu.ticketing.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ASIGNADO_A")
@IdClass(AsignadoA.AsignadoAId.class)
public class AsignadoA {

    @Id
    @Column(name = "direccion_correo_funcionario", length = 150)
    private String emailFuncionario;

    @Id
    @Column(name = "id_encuentro")
    private Integer idEncuentro;

    @Id
    @Column(name = "letra_sector", length = 1)
    private String letraSector;

    @Id
    @Column(name = "id_estadio")
    private Integer idEstadio;

    @ManyToOne
    @JoinColumn(name = "id_encuentro", insertable = false, updatable = false)
    private Encuentro encuentro;

    public String getEmailFuncionario() { return emailFuncionario; }
    public void setEmailFuncionario(String v) { this.emailFuncionario = v; }

    public Integer getIdEncuentro() { return idEncuentro; }
    public void setIdEncuentro(Integer v) { this.idEncuentro = v; }

    public String getLetraSector() { return letraSector; }
    public void setLetraSector(String v) { this.letraSector = v; }

    public Integer getIdEstadio() { return idEstadio; }
    public void setIdEstadio(Integer v) { this.idEstadio = v; }

    public Encuentro getEncuentro() { return encuentro; }

    public static class AsignadoAId implements Serializable {
        private String emailFuncionario;
        private Integer idEncuentro;
        private String letraSector;
        private Integer idEstadio;

        public AsignadoAId() {}

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AsignadoAId a)) return false;
            return emailFuncionario.equals(a.emailFuncionario)
                && idEncuentro.equals(a.idEncuentro)
                && letraSector.equals(a.letraSector)
                && idEstadio.equals(a.idEstadio);
        }

        @Override
        public int hashCode() {
            int r = emailFuncionario.hashCode();
            r = 31 * r + idEncuentro.hashCode();
            r = 31 * r + letraSector.hashCode();
            r = 31 * r + idEstadio.hashCode();
            return r;
        }
    }
}
