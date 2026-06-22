# Modelo Lógico — Pasaje a Tablas

Se documenta el pasaje del MER al modelo relacional. Las claves primarias se indican con **PK**, las foráneas con *FK*.

---

## Herencia de Usuario (estrategia JOINED)

### USUARIO
| Columna | Tipo | Restricciones |
|---|---|---|
| **direccion_correo_electronico** | VARCHAR(150) | PK |
| pais_documento_identidad | VARCHAR(100) | NOT NULL |
| tipo_documento | VARCHAR(50) | NOT NULL |
| numero_documento | VARCHAR(50) | NOT NULL |
| pais_direccion | VARCHAR(100) | NOT NULL |
| localidad | VARCHAR(100) | NOT NULL |
| calle | VARCHAR(150) | NOT NULL |
| numero_direccion | VARCHAR(20) | NOT NULL |
| codigo_postal | VARCHAR(20) | NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |

### TELEFONO
| Columna | Tipo | Restricciones |
|---|---|---|
| **direccion_correo_electronico** | VARCHAR(150) | PK, *FK → USUARIO* |
| **telefono** | VARCHAR(255) | PK |

### ADMINISTRADOR_POR_PAIS_SEDE
| Columna | Tipo | Restricciones |
|---|---|---|
| **direccion_correo_electronico** | VARCHAR(150) | PK, *FK → USUARIO* |
| pais_que_administra | VARCHAR(100) | NOT NULL |
| fecha_asignacion_cargo | DATE | NOT NULL |

### FUNCIONARIO_DE_VALIDACION
| Columna | Tipo | Restricciones |
|---|---|---|
| **direccion_correo_electronico** | VARCHAR(150) | PK, *FK → USUARIO* |
| numero_legajo | VARCHAR(50) | NOT NULL |

### USUARIO_GENERAL
| Columna | Tipo | Restricciones |
|---|---|---|
| **direccion_correo_electronico** | VARCHAR(150) | PK, *FK → USUARIO* |
| estado_verificacion_identidad | VARCHAR(50) | |
| fecha_registro_en_sistema | DATE | NOT NULL |

---

## Infraestructura

### ESTADIO
| Columna | Tipo | Restricciones |
|---|---|---|
| **id_estadio** | INT | PK, AUTO_INCREMENT |
| nombre | VARCHAR(150) | NOT NULL |
| direccion | VARCHAR(255) | NOT NULL |
| pais | VARCHAR(100) | NOT NULL |
| aforo | INT | NOT NULL |

### SECTOR
| Columna | Tipo | Restricciones |
|---|---|---|
| **letra** | VARCHAR(1) | PK |
| **id_estadio** | INT | PK, *FK → ESTADIO* |
| aforo | INT | NOT NULL |
| precio | DECIMAL(10,2) | NOT NULL |

### EQUIPO
| Columna | Tipo | Restricciones |
|---|---|---|
| **pais** | VARCHAR(100) | PK |

### ENCUENTRO
| Columna | Tipo | Restricciones |
|---|---|---|
| **id_encuentro** | INT | PK, AUTO_INCREMENT |
| fecha | DATE | NOT NULL |
| hora | TIME | NOT NULL |
| id_estadio | INT | NOT NULL, *FK → ESTADIO* |
| pais_equipo_local | VARCHAR(100) | NOT NULL, *FK → EQUIPO* |
| pais_equipo_visitante | VARCHAR(100) | NOT NULL, *FK → EQUIPO* |
| direccion_correo_administrador | VARCHAR(150) | NOT NULL, *FK → ADMINISTRADOR_POR_PAIS_SEDE* |

### TIENE_HABILITADO
| Columna | Tipo | Restricciones |
|---|---|---|
| **id_encuentro** | INT | PK, *FK → ENCUENTRO* |
| **letra** | VARCHAR(1) | PK |
| **id_estadio** | INT | PK, *FK → SECTOR* |

---

## Transacciones

### COMPRA
| Columna | Tipo | Restricciones |
|---|---|---|
| **id_compra** | INT | PK, AUTO_INCREMENT |
| fecha | DATE | NOT NULL |
| hora | TIME | NOT NULL |
| estado | ENUM('pendiente','confirmada','paga') | NOT NULL |
| monto_total | DECIMAL(10,2) | NOT NULL |
| comision_aplicada | DECIMAL(5,2) | NOT NULL |
| direccion_correo_usuario | VARCHAR(150) | NOT NULL, *FK → USUARIO_GENERAL* |

### ENTRADA
| Columna | Tipo | Restricciones |
|---|---|---|
| **id_entrada** | INT | PK, AUTO_INCREMENT |
| monto_sector | DECIMAL(10,2) | NOT NULL |
| id_encuentro | INT | NOT NULL, *FK → ENCUENTRO* |
| letra_sector | VARCHAR(1) | NOT NULL |
| id_estadio | INT | NOT NULL, *FK → SECTOR(letra, id_estadio)* |
| propietario_actual | VARCHAR(150) | NOT NULL, *FK → USUARIO_GENERAL* |
| estado | ENUM('activa','consumida','transferida_pendiente') | NOT NULL |
| cantidad_transferencias | TINYINT | NOT NULL, DEFAULT 0 |
| qr_token_actual | VARCHAR(255) | |
| qr_token_expira_en | DATETIME | |

### TRANSFERENCIA
| Columna | Tipo | Restricciones |
|---|---|---|
| **id_transferencia** | INT | PK, AUTO_INCREMENT |
| usuario_emisor | VARCHAR(150) | NOT NULL, *FK → USUARIO_GENERAL* |
| usuario_receptor | VARCHAR(150) | NOT NULL, *FK → USUARIO_GENERAL* |
| id_entrada | INT | NOT NULL, *FK → ENTRADA* |
| fecha | DATE | NOT NULL |
| hora | TIME | NOT NULL |
| fecha_respuesta | DATETIME | |
| estado | ENUM('pendiente','aceptada','rechazada') | NOT NULL |

---

## Validación

### DISPOSITIVO_ESCANEO_AUTORIZADO
| Columna | Tipo | Restricciones |
|---|---|---|
| **id_dispositivo** | VARCHAR(100) | PK |

### TIENE_ASIGNADO
| Columna | Tipo | Restricciones |
|---|---|---|
| **direccion_correo_funcionario** | VARCHAR(150) | PK, *FK → FUNCIONARIO_DE_VALIDACION* |
| **id_dispositivo** | VARCHAR(100) | PK, *FK → DISPOSITIVO_ESCANEO_AUTORIZADO* |

### VALIDA
| Columna | Tipo | Restricciones |
|---|---|---|
| **id_dispositivo** | VARCHAR(100) | PK, *FK → DISPOSITIVO_ESCANEO_AUTORIZADO* |
| **id_entrada** | INT | PK, *FK → ENTRADA* |
| id_encuentro | INT | NOT NULL, *FK → ENCUENTRO* |
| direccion_correo_funcionario | VARCHAR(150) | NOT NULL, *FK → FUNCIONARIO_DE_VALIDACION* |
| codigo_aceptado | VARCHAR(255) | NOT NULL |
| hora | DATETIME | NOT NULL |
