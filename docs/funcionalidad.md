# Funcionalidad del Sistema

## Arquitectura general

El sistema sigue una arquitectura de tres capas:

- **Presentación:** Single Page Application (React + Vite) servida desde Vercel
- **Lógica de negocio:** API REST (Spring Boot) corriendo en Railway
- **Persistencia:** MySQL 8 en el servidor Linux de la UCU

El frontend se comunica con el backend exclusivamente mediante HTTP. La autenticación usa HTTP Basic Auth — el navegador envía en cada request el header `Authorization: Basic <base64(email:password)>`. En producción ambas capas corren sobre HTTPS, por lo que las credenciales no viajan en texto plano.

---

## Roles y control de acceso

| Rol | Cómo se crea | Acceso |
|---|---|---|
| ADMIN | INSERT manual en BD | Gestión de estadios, equipos, encuentros, sectores, rankings |
| FUNCIONARIO | INSERT manual en BD | Validación de entradas con dispositivo autorizado |
| USER | Registro desde la app | Compra, transferencia y visualización de sus entradas |

Spring Security intercepta cada request y determina el rol consultando en qué tabla de herencia existe el email autenticado (ADMINISTRADOR_POR_PAIS_SEDE, FUNCIONARIO_DE_VALIDACION o USUARIO_GENERAL).

---

## Requerimientos funcionales

### RF-01: Registro de usuario
- El usuario ingresa email, documento (país + tipo + número), dirección (país + localidad + calle + número + código postal) y uno o más teléfonos de contacto.
- El sistema valida que el email no esté registrado previamente.
- La contraseña se almacena hasheada con BCrypt.

### RF-02: Autenticación
- Todos los endpoints excepto `/api/auth/registro` requieren autenticación Basic Auth.
- El endpoint `/api/auth/perfil` devuelve el rol del usuario autenticado.

### RF-03: Gestión de estadios
- El administrador puede crear, listar y eliminar estadios.
- Al crear un estadio se generan automáticamente los sectores A, B, C y D con aforo proporcional (aforo_total / 4).
- No se puede eliminar un estadio que tenga encuentros programados.

### RF-04: Gestión de equipos
- El administrador puede crear, listar y eliminar equipos identificados por país.
- No se puede eliminar un equipo que participe en encuentros programados.

### RF-05: Gestión de encuentros
- El administrador puede crear, listar y eliminar encuentros especificando equipos (local y visitante), estadio, fecha y hora.
- **No pueden solaparse dos encuentros en el mismo estadio a la misma fecha y hora.**
- No se puede eliminar un encuentro que ya tenga entradas vendidas.
- Para cada encuentro se habilitan uno o más sectores con precio específico.

### RF-06: Compra de entradas
- Un usuario general puede comprar entre 1 y 5 entradas por transacción.
- El sistema valida que haya aforo disponible en el sector seleccionado (límite duro).
- El monto total se calcula como: `suma(precio_sector × cantidad) × 1.05` (comisión del 5%).
- La comisión aplicada queda registrada en la compra para trazabilidad ante futuros cambios de tasa.
- Cada entrada queda inicialmente bajo la titularidad del usuario comprador.

### RF-07: Transferencia de entradas
- Un usuario puede iniciar una transferencia de una entrada activa hacia otro usuario registrado.
- Al iniciar, la entrada pasa al estado `transferida_pendiente` (no se puede vender, transferir ni validar).
- El receptor puede aceptar o rechazar la transferencia.
- Si acepta: la entrada cambia de propietario y el contador de transferencias se incrementa.
- Si rechaza: la entrada vuelve al estado `activa` con el propietario original.
- Una entrada puede ser transferida como máximo **3 veces** antes de su validación.
- El historial completo de transferencias queda registrado, permitiendo reconstruir la cadena de custodia.

### RF-08: QR dinámico
- El usuario puede ver el token QR de sus entradas activas en la sección "Entradas" del dashboard.
- **El token se regenera automáticamente cada 30 segundos** mientras la pestaña del navegador está abierta (mediante `setInterval` en el frontend).
- El backend genera un UUID aleatorio como token y registra su fecha/hora de expiración.
- Un token expirado es rechazado en la validación.

### RF-09: Validación de acceso
- El funcionario ingresa el ID de la entrada, el ID de su dispositivo autorizado y el token QR visible en la pantalla del usuario.
- El sistema verifica: que la entrada exista, que no esté consumida ni en transferencia pendiente, que el token QR coincida y no haya expirado, y que el dispositivo esté autorizado.
- Si todo es válido: la entrada pasa a estado `consumida` de forma **irreversible**, y se registra en la tabla VALIDA el código aceptado, el funcionario, el encuentro y la hora exacta.

### RF-10: Visualización personal
- El usuario puede ver sus compras, sus transferencias (enviadas y recibidas) y sus entradas activas desde el dashboard.

### RF-11: Rankings
- El administrador puede consultar:
  - Eventos ordenados por cantidad de entradas vendidas.
  - Usuarios ordenados por gasto total acumulado.

---

## Diagrama de componentes

```
┌─────────────────────────────────────────────────────┐
│                    VERCEL (CDN)                      │
│  ┌──────────────────────────────────────────────┐   │
│  │          React SPA (ticketing-front)          │   │
│  │  Login │ Registro │ Dashboard │ Comprar       │   │
│  │  Transferir │ Validacion │ Admin │ Rankings   │   │
│  └──────────────────┬───────────────────────────┘   │
└─────────────────────┼───────────────────────────────┘
                      │ HTTPS + Basic Auth
                      │ (proxy via vercel.json → Railway)
┌─────────────────────┼───────────────────────────────┐
│              RAILWAY (Linux container)               │
│  ┌──────────────────▼───────────────────────────┐   │
│  │         Spring Boot API (puerto 8080)         │   │
│  │                                               │   │
│  │  Controllers:                                 │   │
│  │    AuthController                             │   │
│  │    EntradaController                          │   │
│  │    CompraController                           │   │
│  │    TransferenciaController                    │   │
│  │    ValidacionController                       │   │
│  │    AdminController                            │   │
│  │    RankingController                          │   │
│  │    EncuentroController                        │   │
│  │                                               │   │
│  │  Services:                                    │   │
│  │    AuthService                                │   │
│  │    CompraService                              │   │
│  │    TransferenciaService                       │   │
│  │    ValidacionService                          │   │
│  │                                               │   │
│  │  Spring Data JPA → Repositories               │   │
│  └──────────────────┬───────────────────────────┘   │
└─────────────────────┼───────────────────────────────┘
                      │ JDBC (MySQL Connector/J)
┌─────────────────────┼───────────────────────────────┐
│         SERVIDOR UCU (mysql.reto-ucu.net:50006)      │
│  ┌──────────────────▼───────────────────────────┐   │
│  │              MySQL 8 — XR_Grupo5              │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

---

## Flujo principal: compra y validación de entrada

```
Usuario                 Frontend              Backend               BD
  │                        │                     │                   │
  │── login ──────────────>│                     │                   │
  │                        │── GET /perfil ─────>│                   │
  │                        │<── {rol: USER} ─────│                   │
  │                        │                     │                   │
  │── comprar entrada ────>│                     │                   │
  │                        │── POST /compras ────>│                  │
  │                        │                     │── INSERT COMPRA ─>│
  │                        │                     │── INSERT ENTRADA ─>│
  │                        │<── compra creada ───│                   │
  │                        │                     │                   │
  │── ver entradas ───────>│                     │                   │
  │                        │── GET /entradas ────>│                  │
  │                        │   (auto-genera QR) ─>│── UPDATE ENTRADA>│
  │<── token QR visible ───│                     │                   │
  │                        │                     │                   │
Funcionario              Frontend              Backend               BD
  │── ingresar token ─────>│                     │                   │
  │                        │── POST /escanear ───>│                  │
  │                        │                     │── SELECT ENTRADA ─>│
  │                        │                     │── UPDATE consumida>│
  │                        │                     │── INSERT VALIDA ──>│
  │<── "Entrada validada" ─│                     │                   │
```
