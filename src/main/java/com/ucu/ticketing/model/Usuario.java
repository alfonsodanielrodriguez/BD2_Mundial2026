package com.ucu.ticketing.model;

import jakarta.persistence.*;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


@Entity
@Table(name = "USUARIO")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario {

    @Id
    @Column(name = "direccion_correo_electronico", length = 150)
    private String email;

    @JsonIgnore
    @Column(name = "pais_documento_identidad", nullable = false, length = 100)
    private String paisDocumentoIdentidad;

    @JsonIgnore
    @Column(name = "tipo_documento", nullable = false, length = 50)
    private String tipoDocumento;

    @JsonIgnore
    @Column(name = "numero_documento", nullable = false, length = 50)
    private String numeroDocumento;

    @JsonIgnore
    @Column(name = "pais_direccion", nullable = false, length = 100)
    private String paisDireccion;

    @JsonIgnore
    @Column(name = "localidad", nullable = false, length = 100)
    private String localidad;

    @JsonIgnore
    @Column(name = "calle", nullable = false, length = 150)
    private String calle;

    @JsonIgnore
    @Column(name = "numero_direccion", nullable = false, length = 20)
    private String numeroDireccion;

    @JsonIgnore
    @Column(name = "codigo_postal", nullable = false, length = 20)
    private String codigoPostal;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @JsonIgnore
    @ElementCollection
    @CollectionTable(
        name = "TELEFONO",
        joinColumns = @JoinColumn(name = "direccion_correo_electronico")
    )
    @Column(name = "telefono")
    private List<String> telefonos;

    // Getters y setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPaisDocumentoIdentidad() { return paisDocumentoIdentidad; }
    public void setPaisDocumentoIdentidad(String v) { this.paisDocumentoIdentidad = v; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String v) { this.tipoDocumento = v; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String v) { this.numeroDocumento = v; }

    public String getPaisDireccion() { return paisDireccion; }
    public void setPaisDireccion(String v) { this.paisDireccion = v; }

    public String getLocalidad() { return localidad; }
    public void setLocalidad(String v) { this.localidad = v; }

    public String getCalle() { return calle; }
    public void setCalle(String v) { this.calle = v; }

    public String getNumeroDireccion() { return numeroDireccion; }
    public void setNumeroDireccion(String v) { this.numeroDireccion = v; }

    public String getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(String v) { this.codigoPostal = v; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { this.passwordHash = v; }

    public List<String> getTelefonos() { return telefonos; }
    public void setTelefonos(List<String> v) { this.telefonos = v; }
}