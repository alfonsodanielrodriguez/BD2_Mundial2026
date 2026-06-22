# Alternativas de Solución

## Alternativa 1 (Implementada): Aplicación Web — Java Spring Boot + React + MySQL

### Descripción
Sistema cliente/servidor con backend REST en Java 21 + Spring Boot 3.5 y frontend SPA en React + Vite. La base de datos es MySQL 8, alojada en el servidor Linux de la UCU. La comunicación entre capas se realiza mediante HTTP con autenticación Basic Auth. El modelo de datos utiliza herencia de tablas (JOINED) para representar los distintos perfiles de usuario.

### Discusión
- **Ventajas:** separación clara de responsabilidades, API REST reutilizable, ecosistema maduro, despliegue independiente de frontend y backend, soporte nativo de Spring Data JPA para herencia de entidades.
- **Desventajas:** Basic Auth no es ideal para producción real (se prefiere JWT); requiere mantener dos proyectos separados.

### Justificación de la elección
Esta alternativa fue seleccionada por varios motivos. En primer lugar, el equipo contaba con experiencia previa en Java y Spring Boot adquirida en materias anteriores de la carrera de Ingeniería en Informática (Programación 3, Arquitectura de Software), lo que redujo significativamente la curva de aprendizaje y permitió enfocarse en el dominio del problema. En segundo lugar, Spring Data JPA ofrece soporte nativo para el mapeo objeto-relacional y la herencia de entidades, que era un requerimiento central del modelo. Por último, React permitió construir una interfaz reactiva y moderna sin necesidad de infraestructura adicional.

---

## Alternativa 2: Aplicación de Escritorio — .NET WinForms + SQL Server

### Descripción
Sistema de escritorio desarrollado en C# con .NET 8 y Windows Forms. La base de datos sería SQL Server Express (versión gratuita), corriendo en Linux mediante Docker. La lógica de acceso a datos se implementaría con Entity Framework Core.

### Discusión
- **Ventajas:** desarrollo rápido de interfaces con el diseñador visual de WinForms, Entity Framework Core es equivalente a Spring Data JPA en madurez, SQL Server tiene herramientas de diagnóstico muy completas.
- **Desventajas:** la aplicación quedaría restringida a Windows, lo que contradice el espíritu multiusuario y multiplataforma del sistema. No es accesible desde navegador, requiere instalación en cada máquina cliente. La consigna pide modalidad cliente/servidor, pero una app de escritorio conectándose directamente a la BD no es la arquitectura más adecuada para alta concurrencia.

---

## Alternativa 3: Aplicación Web — Node.js + Express + PostgreSQL

### Descripción
Stack completamente JavaScript: backend con Node.js + Express + Sequelize ORM, frontend con React (igual que la alternativa implementada), base de datos PostgreSQL en Linux.

### Discusión
- **Ventajas:** un único lenguaje (JavaScript/TypeScript) en toda la stack reduce la fragmentación cognitiva. PostgreSQL es más robusto que MySQL para consultas complejas y tiene mejor soporte para tipos de datos avanzados. Node.js maneja bien la concurrencia I/O-bound.
- **Desventajas:** Sequelize tiene un soporte más limitado que Hibernate/JPA para herencia de tablas, lo que obligaría a implementar manualmente el patrón JOINED que la consigna requiere. Además, el equipo no contaba con experiencia previa en Node.js en el contexto académico, lo que habría implicado un aprendizaje paralelo al desarrollo del sistema.
