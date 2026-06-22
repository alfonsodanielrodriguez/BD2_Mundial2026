package com.ucu.ticketing.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "VALIDA")
@IdClass(Valida.ValidaId.class)
public class Valida {

    @Id
    @Column(name = "id_dispositivo", length = 100)
    private String idDispositivo;

    @Id
    @Column(name = "id_entrada")
    private Integer idEntrada;

    @ManyToOne
    @JoinColumn(name = "id_dispositivo", insertable = false, updatable = false)
    private DispositivoEscaneo dispositivo;

    @ManyToOne
    @JoinColumn(name = "id_entrada", insertable = false, updatable = false)
    private Entrada entrada;

    @ManyToOne
    @JoinColumn(name = "id_encuentro", nullable = false)
    private Encuentro encuentro;

    @ManyToOne
    @JoinColumn(name = "direccion_correo_funcionario", nullable = false)
    private FuncionarioValidacion funcionario;

    @Column(name = "codigo_aceptado", nullable = false, length = 255)
    private String codigoAceptado;

    @Column(name = "hora", nullable = false)
    private LocalDateTime hora;

    public String getIdDispositivo() { return idDispositivo; }
    public void setIdDispositivo(String v) { this.idDispositivo = v; }

    public Integer getIdEntrada() { return idEntrada; }
    public void setIdEntrada(Integer v) { this.idEntrada = v; }

    public DispositivoEscaneo getDispositivo() { return dispositivo; }
    public void setDispositivo(DispositivoEscaneo v) { this.dispositivo = v; }

    public Entrada getEntrada() { return entrada; }
    public void setEntrada(Entrada v) { this.entrada = v; }

    public Encuentro getEncuentro() { return encuentro; }
    public void setEncuentro(Encuentro v) { this.encuentro = v; }

    public FuncionarioValidacion getFuncionario() { return funcionario; }
    public void setFuncionario(FuncionarioValidacion v) { this.funcionario = v; }

    public String getCodigoAceptado() { return codigoAceptado; }
    public void setCodigoAceptado(String v) { this.codigoAceptado = v; }

    public LocalDateTime getHora() { return hora; }
    public void setHora(LocalDateTime v) { this.hora = v; }

    public static class ValidaId implements Serializable {
        private String idDispositivo;
        private Integer idEntrada;

        public ValidaId() {}

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ValidaId v)) return false;
            return idDispositivo.equals(v.idDispositivo) && idEntrada.equals(v.idEntrada);
        }

        @Override
        public int hashCode() {
            return 31 * idDispositivo.hashCode() + idEntrada.hashCode();
        }
    }
}