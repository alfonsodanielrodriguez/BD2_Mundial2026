-- ============================================================
-- Script de creación de la base de datos
-- Sistema de Ticketing Mundial 2026 — BD2 UCU Grupo 5
-- MySQL 8
-- ============================================================

-- Herencia de Usuario (estrategia JOINED)

CREATE TABLE USUARIO (
    direccion_correo_electronico VARCHAR(150) NOT NULL,
    pais_documento_identidad     VARCHAR(100) NOT NULL,
    tipo_documento               VARCHAR(50)  NOT NULL,
    numero_documento             VARCHAR(50)  NOT NULL,
    pais_direccion               VARCHAR(100) NOT NULL,
    localidad                    VARCHAR(100) NOT NULL,
    calle                        VARCHAR(150) NOT NULL,
    numero_direccion             VARCHAR(20)  NOT NULL,
    codigo_postal                VARCHAR(20)  NOT NULL,
    password_hash                VARCHAR(255) NOT NULL,
    PRIMARY KEY (direccion_correo_electronico)
);

CREATE TABLE TELEFONO (
    direccion_correo_electronico VARCHAR(150) NOT NULL,
    telefono                     VARCHAR(255) NOT NULL,
    PRIMARY KEY (direccion_correo_electronico, telefono),
    FOREIGN KEY (direccion_correo_electronico) REFERENCES USUARIO(direccion_correo_electronico)
);

CREATE TABLE ADMINISTRADOR_POR_PAIS_SEDE (
    direccion_correo_electronico VARCHAR(150) NOT NULL,
    pais_que_administra          VARCHAR(100) NOT NULL,
    fecha_asignacion_cargo       DATE         NOT NULL,
    PRIMARY KEY (direccion_correo_electronico),
    FOREIGN KEY (direccion_correo_electronico) REFERENCES USUARIO(direccion_correo_electronico)
);

CREATE TABLE FUNCIONARIO_DE_VALIDACION (
    direccion_correo_electronico VARCHAR(150) NOT NULL,
    numero_legajo                VARCHAR(50)  NOT NULL,
    PRIMARY KEY (direccion_correo_electronico),
    FOREIGN KEY (direccion_correo_electronico) REFERENCES USUARIO(direccion_correo_electronico)
);

CREATE TABLE USUARIO_GENERAL (
    direccion_correo_electronico    VARCHAR(150) NOT NULL,
    estado_verificacion_identidad   VARCHAR(50),
    fecha_registro_en_sistema       DATE         NOT NULL,
    PRIMARY KEY (direccion_correo_electronico),
    FOREIGN KEY (direccion_correo_electronico) REFERENCES USUARIO(direccion_correo_electronico)
);

-- Infraestructura

CREATE TABLE ESTADIO (
    id_estadio  INT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(150) NOT NULL,
    direccion   VARCHAR(255) NOT NULL,
    pais        VARCHAR(100) NOT NULL,
    aforo       INT          NOT NULL,
    PRIMARY KEY (id_estadio)
);

CREATE TABLE SECTOR (
    letra       VARCHAR(1)      NOT NULL,
    id_estadio  INT             NOT NULL,
    aforo       INT             NOT NULL,
    precio      DECIMAL(10,2)   NOT NULL,
    PRIMARY KEY (letra, id_estadio),
    FOREIGN KEY (id_estadio) REFERENCES ESTADIO(id_estadio)
);

CREATE TABLE EQUIPO (
    pais VARCHAR(100) NOT NULL,
    PRIMARY KEY (pais)
);

CREATE TABLE ENCUENTRO (
    id_encuentro                    INT          NOT NULL AUTO_INCREMENT,
    fecha                           DATE         NOT NULL,
    hora                            TIME         NOT NULL,
    id_estadio                      INT          NOT NULL,
    pais_equipo_local               VARCHAR(100) NOT NULL,
    pais_equipo_visitante           VARCHAR(100) NOT NULL,
    direccion_correo_administrador  VARCHAR(150) NOT NULL,
    PRIMARY KEY (id_encuentro),
    FOREIGN KEY (id_estadio)                     REFERENCES ESTADIO(id_estadio),
    FOREIGN KEY (pais_equipo_local)              REFERENCES EQUIPO(pais),
    FOREIGN KEY (pais_equipo_visitante)          REFERENCES EQUIPO(pais),
    FOREIGN KEY (direccion_correo_administrador) REFERENCES ADMINISTRADOR_POR_PAIS_SEDE(direccion_correo_electronico)
);

CREATE TABLE TIENE_HABILITADO (
    id_encuentro    INT         NOT NULL,
    letra           VARCHAR(1)  NOT NULL,
    id_estadio      INT         NOT NULL,
    PRIMARY KEY (id_encuentro, letra, id_estadio),
    FOREIGN KEY (id_encuentro)          REFERENCES ENCUENTRO(id_encuentro),
    FOREIGN KEY (letra, id_estadio)     REFERENCES SECTOR(letra, id_estadio)
);

-- Transacciones

CREATE TABLE COMPRA (
    id_compra               INT             NOT NULL AUTO_INCREMENT,
    fecha                   DATE            NOT NULL,
    hora                    TIME            NOT NULL,
    estado                  ENUM('pendiente','confirmada','paga') NOT NULL DEFAULT 'pendiente',
    monto_total             DECIMAL(10,2)   NOT NULL,
    comision_aplicada       DECIMAL(5,2)    NOT NULL DEFAULT 5.00,
    direccion_correo_usuario VARCHAR(150)   NOT NULL,
    PRIMARY KEY (id_compra),
    FOREIGN KEY (direccion_correo_usuario) REFERENCES USUARIO_GENERAL(direccion_correo_electronico)
);

CREATE TABLE ENTRADA (
    id_entrada              INT             NOT NULL AUTO_INCREMENT,
    monto_sector            DECIMAL(10,2)   NOT NULL,
    id_encuentro            INT             NOT NULL,
    letra_sector            VARCHAR(1)      NOT NULL,
    id_estadio              INT             NOT NULL,
    propietario_actual      VARCHAR(150)    NOT NULL,
    estado                  ENUM('activa','consumida','transferida_pendiente') NOT NULL DEFAULT 'activa',
    cantidad_transferencias TINYINT         NOT NULL DEFAULT 0,
    qr_token_actual         VARCHAR(255),
    qr_token_expira_en      DATETIME,
    PRIMARY KEY (id_entrada),
    FOREIGN KEY (id_encuentro)              REFERENCES ENCUENTRO(id_encuentro),
    FOREIGN KEY (letra_sector, id_estadio)  REFERENCES SECTOR(letra, id_estadio),
    FOREIGN KEY (propietario_actual)        REFERENCES USUARIO_GENERAL(direccion_correo_electronico)
);

CREATE TABLE TRANSFERENCIA (
    id_transferencia    INT         NOT NULL AUTO_INCREMENT,
    usuario_emisor      VARCHAR(150) NOT NULL,
    usuario_receptor    VARCHAR(150) NOT NULL,
    id_entrada          INT         NOT NULL,
    fecha               DATE        NOT NULL,
    hora                TIME        NOT NULL,
    fecha_respuesta     DATETIME,
    estado              ENUM('pendiente','aceptada','rechazada') NOT NULL DEFAULT 'pendiente',
    PRIMARY KEY (id_transferencia),
    FOREIGN KEY (usuario_emisor)    REFERENCES USUARIO_GENERAL(direccion_correo_electronico),
    FOREIGN KEY (usuario_receptor)  REFERENCES USUARIO_GENERAL(direccion_correo_electronico),
    FOREIGN KEY (id_entrada)        REFERENCES ENTRADA(id_entrada)
);

-- Validación

CREATE TABLE DISPOSITIVO_ESCANEO_AUTORIZADO (
    id_dispositivo VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_dispositivo)
);

CREATE TABLE TIENE_ASIGNADO (
    direccion_correo_funcionario    VARCHAR(150) NOT NULL,
    id_dispositivo                  VARCHAR(100) NOT NULL,
    PRIMARY KEY (direccion_correo_funcionario, id_dispositivo),
    FOREIGN KEY (direccion_correo_funcionario) REFERENCES FUNCIONARIO_DE_VALIDACION(direccion_correo_electronico),
    FOREIGN KEY (id_dispositivo)               REFERENCES DISPOSITIVO_ESCANEO_AUTORIZADO(id_dispositivo)
);

CREATE TABLE VALIDA (
    id_dispositivo                  VARCHAR(100) NOT NULL,
    id_entrada                      INT          NOT NULL,
    id_encuentro                    INT          NOT NULL,
    direccion_correo_funcionario    VARCHAR(150) NOT NULL,
    codigo_aceptado                 VARCHAR(255) NOT NULL,
    hora                            DATETIME     NOT NULL,
    PRIMARY KEY (id_dispositivo, id_entrada),
    FOREIGN KEY (id_dispositivo)               REFERENCES DISPOSITIVO_ESCANEO_AUTORIZADO(id_dispositivo),
    FOREIGN KEY (id_entrada)                   REFERENCES ENTRADA(id_entrada),
    FOREIGN KEY (id_encuentro)                 REFERENCES ENCUENTRO(id_encuentro),
    FOREIGN KEY (direccion_correo_funcionario) REFERENCES FUNCIONARIO_DE_VALIDACION(direccion_correo_electronico)
);

-- ============================================================
-- Datos iniciales de prueba
-- ============================================================

-- Usuario administrador (password: test123)
INSERT INTO USUARIO VALUES (
    'admin@ucu.edu.uy', 'Uruguay', 'CI', '00000001',
    'Uruguay', 'Montevideo', 'Av. 8 de Octubre', '2738', '11600',
    '$2a$10$AYCohUu44ENxpz7qJ/l8vuO36fzJTG0HeOKqperj4ruZFgZl3KLC.'
);
INSERT INTO ADMINISTRADOR_POR_PAIS_SEDE VALUES ('admin@ucu.edu.uy', 'Uruguay', '2026-01-01');

-- Usuario funcionario (password: test123)
INSERT INTO USUARIO VALUES (
    'funcionario@ucu.edu.uy', 'Uruguay', 'CI', '00000002',
    'Uruguay', 'Montevideo', 'Av. Italia', '1234', '11600',
    '$2a$10$AYCohUu44ENxpz7qJ/l8vuO36fzJTG0HeOKqperj4ruZFgZl3KLC.'
);
INSERT INTO FUNCIONARIO_DE_VALIDACION VALUES ('funcionario@ucu.edu.uy', 'LEG-001');

-- Dispositivo autorizado
INSERT INTO DISPOSITIVO_ESCANEO_AUTORIZADO VALUES ('DISP-001');
INSERT INTO TIENE_ASIGNADO VALUES ('funcionario@ucu.edu.uy', 'DISP-001');
