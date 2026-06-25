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

    @ManyToOne
    @JoinColumn(name = "id_encuentro", insertable = false, updatable = false)
    private Encuentro encuentro;

    public String getEmailFuncionario() { return emailFuncionario; }
    public void setEmailFuncionario(String v) { this.emailFuncionario = v; }

    public Integer getIdEncuentro() { return idEncuentro; }
    public void setIdEncuentro(Integer v) { this.idEncuentro = v; }

    public Encuentro getEncuentro() { return encuentro; }

    public static class AsignadoAId implements Serializable {
        private String emailFuncionario;
        private Integer idEncuentro;
        public AsignadoAId() {}
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AsignadoAId a)) return false;
            return emailFuncionario.equals(a.emailFuncionario) && idEncuentro.equals(a.idEncuentro);
        }
        @Override
        public int hashCode() {
            return 31 * emailFuncionario.hashCode() + idEncuentro.hashCode();
        }
    }
}
