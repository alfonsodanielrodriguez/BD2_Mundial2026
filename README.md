# Sistema de Ticketing — Mundial 2026
**BD2 UCU — Grupo 5**

Sistema integral de ticketing para la comercialización, transferencia y validación de entradas para los partidos del Mundial 2026.

---

## Acceso a la aplicación hosteada

| Componente | URL |
|---|---|
| Frontend (producción) | https://ticketing-front-black.vercel.app |
| Backend API | https://bd2mundial2026-production.up.railway.app |

### Usuarios de prueba

| Email | Password | Rol |
|---|---|---|
| admin@ucu.edu.uy | test123 | Administrador |
| funcionario@ucu.edu.uy | test123 | Funcionario de validación |
| anegreira@prueba.com | test123 | Usuario general |

---

## Flujo de prueba completo

1. **Loguearse como ADMIN** → crear equipos, un estadio, un encuentro → habilitar sectores con precio
2. **Como ADMIN** → en la tab "Funcionarios" → asignar el funcionario al sector habilitado del encuentro creado
3. **Registrar un USER** desde la pantalla de registro
4. **Como USER** → comprar entradas: seleccioná encuentro/sector/cantidad → "Reservar" → "Confirmar compra"
5. Opcionalmente: **transferir** una entrada a otro usuario registrado y aceptarla
6. **En la tab "Entradas"** → el token QR aparece automáticamente y se renueva cada 30 segundos
7. **Loguearse como FUNCIONARIO** → ir a "Validar entrada" → ingresar el token QR visible en la pantalla del usuario
8. Verificar que la entrada queda como **consumida**
9. **Como ADMIN** → ver Rankings

---

## Ejecutar localmente

### Requisitos
- Java 21
- Maven 3.9+
- Node.js 18+
- Credenciales de la BD MySQL del grupo (pedirlas a un integrante del equipo)

### Backend

El archivo `src/main/resources/application.properties` ya existe en el repo y contiene la configuración base (no tocar). Para correr localmente hay que crear un archivo **adicional** que no se commitea al repo:

`src/main/resources/application-local.properties`

```properties
spring.datasource.url=jdbc:mysql://mysql.reto-ucu.net:50006/XR_Grupo5?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=xr_g5_admin
spring.datasource.password=PEDIR_AL_EQUIPO
```

> Este archivo está en `.gitignore` por seguridad. Nunca commitear credenciales.

Luego ejecutar con el perfil local:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

El backend queda disponible en `http://localhost:8080`.

### Frontend

```bash
cd ticketing-front
npm install
npm run dev
```

El frontend queda disponible en `http://localhost:5173`. El proxy de Vite redirige automáticamente las llamadas a `/api/*` al backend en el puerto 8080, por lo que no hay que cambiar ninguna URL.

### Tests

```bash
./mvnw test
```

Se ejecutan 30 tests unitarios sobre la lógica de negocio (CompraService, TransferenciaService, ValidacionService, AuthService). No requieren conexión a la base de datos ni el archivo `application-local.properties`.

---

## Estructura del proyecto

```
ticketing/
├── src/main/java/com/ucu/ticketing/
│   ├── model/          # Entidades JPA (herencia JOINED)
│   ├── repository/     # Interfaces Spring Data JPA
│   ├── service/        # Lógica de negocio
│   ├── controller/     # Endpoints REST
│   └── security/       # Spring Security + CORS
├── src/test/           # Tests unitarios (JUnit 5 + Mockito)
├── docs/               # Documentación del informe
│   ├── alternativas.md
│   ├── modelo_logico.md
│   ├── script_bd.sql
│   ├── funcionalidad.md
│   └── conclusiones.md
└── ticketing-front/    # Aplicación React
    ├── src/pages/      # Login, Registro, Dashboard, Comprar,
    │                   # Transferir, Validacion, Admin, Rankings
    └── vercel.json     # Proxy /api → Railway
```

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 21 + Spring Boot 3.5 + Spring Security + Spring Data JPA |
| Frontend | React 18 + Vite + Tailwind CSS + Axios |
| Base de datos | MySQL 8 (servidor UCU: mysql.reto-ucu.net:50006) |
| Hosting backend | Railway |
| Hosting frontend | Vercel |
| Tests | JUnit 5 + Mockito |
