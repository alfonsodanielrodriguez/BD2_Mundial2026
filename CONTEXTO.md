# DISCLAIMER 1: 
Este contexto.md fue generado por una instancia de chat de claude que ayudo a construir todo este proyecto, pero la misma pudo haber generado errores o perdido contexto a la hora de generar el contexto debido a su uso prolongado.
Por esto mismo se pide este resumen fuertemente orientativo para poder trabajar con una intancia de un agente de claude code (tu) para continuar realizando modificaciones en caso de ser necesario y generar casos de prueba, pero sin dejar de validar tu mismo contra los archivos del codigo fuente del proyecto que la informacion este bien.

# DISCLAIMER 2: 
Te adjunto el md (en utf latin 1) de la consigna de este proyecto para que puedas validar todos los requerimientos establecidos y poder hacer que se cumplan todos. La instancia de chat me dijo que lo que falta o esta incompleto es lo siguiente, asi que puedes solicitarme confirmacion y trabajar en esto:

Falta o está incompleto:

Superposición de eventos en mismo recinto — la letra dice que no pueden solaparse dos encuentros en el mismo estadio en la misma fecha/hora. No está validado en el backend.
Telefonos en registro — el formulario de registro no permite agregar teléfonos.
QR se regenera automáticamente cada 30s — actualmente es manual (botón). La letra dice "mientras la app esté en primer plano".

# Contexto del Proyecto — BD2 Obligatorio 2026

## Descripción
Sistema de ticketing para el Mundial 2026. Trabajo obligatorio universitario (UCU - BD2).

## Stack
- **Backend:** Java 21 + Spring Boot 3.5 + Spring Security (Basic Auth) + Spring Data JPA
- **Frontend:** React + Vite + Tailwind CSS + Axios
- **BD:** MySQL 8 (servidor UCU: mysql.reto-ucu.net:50006, base: XR_Grupo5)

## Estructura del proyecto
```
ticketing/                  ← raíz
├── src/main/java/com/ucu/ticketing/
│   ├── model/              ← entidades JPA
│   ├── repository/         ← interfaces JPA
│   ├── service/            ← lógica de negocio
│   ├── controller/         ← endpoints REST
│   └── security/           ← Spring Security config
├── src/main/resources/
│   └── application.properties
└── ticketing-front/        ← proyecto React
    └── src/pages/          ← Login, Registro, Dashboard, Admin, Comprar, Transferir, Validacion, Rankings
```

## Roles de usuario
| Rol | Descripción | Cómo se crea |
|-----|-------------|--------------|
| ADMIN | Gestiona estadios, equipos, encuentros, sectores | INSERT manual en BD |
| FUNCIONARIO | Valida entradas con dispositivo | INSERT manual en BD |
| USER | Compra, transfiere y ve sus entradas | Registro desde la app |

## Usuarios de prueba (password: test123, hash: $2a$10$AYCohUu44ENxpz7qJ/l8vuO36fzJTG0HeOKqperj4ruZFgZl3KLC.)
- admin@ucu.edu.uy → ADMIN
- funcionario@ucu.edu.uy → FUNCIONARIO (legajo LEG-001, dispositivo DISP-001)
- cualquier registro desde la app → USER

## Modelos principales

### Herencia de Usuario (JOINED)
- `USUARIO` (base): email, documento, dirección, password_hash
- `ADMINISTRADOR_POR_PAIS_SEDE`: pais_que_administra, fecha_asignacion_cargo
- `FUNCIONARIO_DE_VALIDACION`: numero_legajo
- `USUARIO_GENERAL`: estado_verificacion_identidad, fecha_registro_en_sistema

### Infraestructura
- `ESTADIO`: id, nombre, direccion, pais, aforo
- `SECTOR`: (letra, id_estadio) PK compuesta, aforo, precio — siempre A,B,C,D por estadio
- `EQUIPO`: pais (PK)
- `ENCUENTRO`: id, fecha, hora, estadio, equipo_local, equipo_visitante, administrador
- `TIENE_HABILITADO`: (id_encuentro, letra, id_estadio) — sectores habilitados por encuentro

### Transacciones
- `COMPRA`: id, fecha, hora, estado (pendiente/confirmada/paga), monto_total, comision_aplicada
- `ENTRADA`: id, monto_sector, encuentro, sector, propietario_actual, estado (activa/consumida/transferida_pendiente), cantidad_transferencias (max 3), qr_token_actual, qr_token_expira_en
- `CONTIENE`: (id_compra, id_entrada)
- `TRANSFERENCIA`: id, emisor, receptor, entrada, fecha, hora, fecha_respuesta, estado (pendiente/aceptada/rechazada)

### Validación
- `DISPOSITIVO_ESCANEO_AUTORIZADO`: id_dispositivo
- `TIENE_ASIGNADO`: (funcionario, dispositivo)
- `VALIDA`: (dispositivo, entrada), encuentro, funcionario, codigo_aceptado, hora

## Endpoints REST

### Auth
- POST /api/auth/registro — registro de usuario general
- GET /api/auth/perfil — devuelve {email, rol: ADMIN|FUNCIONARIO|USER}

### Entradas
- GET /api/entradas — mis entradas (con datos de encuentro)
- POST /api/entradas/{id}/generar-qr — genera token QR (5 min en dev, 30s en prod)
- GET /api/entradas/{id}/qr-token — obtiene token actual

### Compras
- POST /api/compras — comprar entradas (max 5 por transacción, valida aforo)
- GET /api/compras — mis compras

### Transferencias
- POST /api/transferencias — iniciar transferencia
- PUT /api/transferencias/{id}/responder — aceptar/rechazar {aceptar: true/false}
- GET /api/transferencias — mis transferencias (enviadas y recibidas)

### Admin
- GET/POST /api/admin/equipos
- DELETE /api/admin/equipos/{pais} — restringido si tiene encuentros
- GET/POST /api/admin/estadios
- DELETE /api/admin/estadios/{id} — restringido si tiene encuentros, borra sectores
- GET/POST /api/admin/encuentros — restringido si tiene entradas vendidas
- DELETE /api/admin/encuentros/{id}
- GET /api/admin/encuentros/{id}/sectores — sectores habilitados
- POST /api/admin/encuentros/{id}/sectores — habilitar sector {letra, precio}
- DELETE /api/admin/encuentros/{id}/sectores/{letra} — deshabilitar sector

### Encuentros públicos
- GET /api/encuentros — lista todos los encuentros

### Validación
- POST /api/validacion/escanear — {idEntrada, idDispositivo, codigoQr}

### Rankings
- GET /api/rankings/eventos — encuentros ordenados por entradas vendidas
- GET /api/rankings/compradores — usuarios ordenados por gasto total

## Flujo de prueba completo
1. Loguearse como ADMIN → crear equipo, estadio, encuentro → habilitar sectores con precio
2. Registrar un USER → comprar entradas para ese encuentro
3. Transferir entrada a otro USER → aceptar transferencia
4. Como USER propietario → generar QR de entrada activa
5. Loguearse como FUNCIONARIO → validar entrada con ID + DISP-001 + token QR
6. Verificar que la entrada queda como "consumida"
7. Como ADMIN → ver rankings

## Reglas de negocio implementadas
- Máximo 5 entradas por compra
- Máximo 3 transferencias por entrada antes de validación
- Entrada consumida es irreversible
- No se puede transferir una entrada en estado transferida_pendiente
- No se puede eliminar estadio con encuentros / encuentro con entradas vendidas
- Sectores tienen aforo máximo (límite duro)
- QR expira en 30 segundos (5 minutos en desarrollo)

## Pendiente / mejoras futuras
- DTOs para respuestas más limpias
- Deploy: frontend en Vercel, backend en Railway
- Implementación real de QR visual (actualmente es token UUID)
- Tests automatizados
