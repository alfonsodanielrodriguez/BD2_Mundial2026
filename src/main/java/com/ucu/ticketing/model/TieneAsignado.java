package com.ucu.ticketing.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "TIENE_ASIGNADO")
@IdClass(TieneAsignado.TieneAsignadoId.class)
public class TieneAsignado {

    @Id
    @Column(name = "direccion_correo_funcionario", length = 150)
    private String emailFuncionario;

    @Id
    @Column(name = "id_dispositivo", length = 100)
    private String idDispositivo;

    @ManyToOne
    @JoinColumn(name = "direccion_correo_funcionario", insertable = false, updatable = false)
    private FuncionarioValidacion funcionario;

    @ManyToOne
    @JoinColumn(name = "id_dispositivo", insertable = false, updatable = false)
    private DispositivoEscaneo dispositivo;

    public String getEmailFuncionario() { return emailFuncionario; }
    public void setEmailFuncionario(String v) { this.emailFuncionario = v; }

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String v) { this.idDispositivo = v; }

    public static class TieneAsignadoId implements Serializable {
        private String emailFuncionario;
        private String idDispositivo;

        public TieneAsignadoId() {}

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TieneAsignadoId t)) return false;
            return emailFuncionario.equals(t.emailFuncionario) && idDispositivo.equals(t.idDispositivo);
        }

        @Override
        public int hashCode() {
            return 31 * emailFuncionario.hashCode() + idDispositivo.hashCode();
        }
    }
}