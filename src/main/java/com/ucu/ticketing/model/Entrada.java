package com.ucu.ticketing.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ENTRADA")
public class Entrada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrada")
    private Integer idEntrada;

    @Column(name = "monto_sector", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoSector;

    @ManyToOne
    @JoinColumn(name = "id_encuentro", nullable = false)
    private Encuentro encuentro;

    @Column(name = "letra_sector", nullable = false, length = 1)
    private String letraSector;

    @Column(name = "id_estadio", nullable = false)
    private Integer idEstadio;

    @JsonIgnore
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "letra_sector", referencedColumnName = "letra", insertable = false, updatable = false),
        @JoinColumn(name = "id_estadio", referencedColumnName = "id_estadio", insertable = false, updatable = false)
    })
    private Sector sector;

    @ManyToOne
    @JoinColumn(name = "propietario_actual", nullable = false)
    private UsuarioGeneral propietarioActual;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado = Estado.activa;

    @Column(name = "cantidad_transferencias", nullable = false)
    private Byte cantidadTransferencias = 0;

    @JsonIgnore
    @Column(name = "qr_token_actual", length = 255)
    private String qrTokenActual;

    @JsonIgnore
    @Column(name = "qr_token_expira_en")
    private LocalDateTime qrTokenExpiraEn;

    public enum Estado { activa, consumida, transferida_pendiente }

    public Integer getIdEntrada() { return idEntrada; }
    public void setIdEntrada(Integer v) { this.idEntrada = v; }

    public BigDecimal getMontoSector() { return montoSector; }
    public void setMontoSector(BigDecimal v) { this.montoSector = v; }

    public Encuentro getEncuentro() { return encuentro; }
    public void setEncuentro(Encuentro v) { this.encuentro = v; }

    public String getLetraSector() { return letraSector; }
    public void setLetraSector(String v) { this.letraSector = v; }

    public Integer getIdEstadio() { return idEstadio; }
    public void setIdEstadio(Integer v) { this.idEstadio = v; }

    public Sector getSector() { return sector; }
    public void setSector(Sector v) { this.sector = v; }

    public UsuarioGeneral getPropietarioActual() { return propietarioActual; }
    public void setPropietarioActual(UsuarioGeneral v) { this.propietarioActual = v; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado v) { this.estado = v; }

    public Byte getCantidadTransferencias() { return cantidadTransferencias; }
    public void setCantidadTransferencias(Byte v) { this.cantidadTransferencias = v; }

    public String getQrTokenActual() { return qrTokenActual; }
    public void setQrTokenActual(String v) { this.qrTokenActual = v; }

    public LocalDateTime getQrTokenExpiraEn() { return qrTokenExpiraEn; }
    public void setQrTokenExpiraEn(LocalDateTime v) { this.qrTokenExpiraEn = v; }
}