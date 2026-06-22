# Conclusiones

## Aspectos más importantes del trabajo

El desarrollo del sistema de ticketing para el Mundial 2026 permitió integrar los conceptos centrales de la materia Bases de Datos II en un problema real y complejo: herencia de entidades en el modelo relacional, integridad referencial con claves compuestas, control de concurrencia mediante transacciones, y diseño de un esquema extensible orientado a futuras estadísticas y reportes.

El aspecto más desafiante fue el diseño del modelo de herencia de usuarios. La estrategia JOINED (una tabla por clase) fue elegida sobre SINGLE_TABLE y TABLE_PER_CLASS porque mantiene la integridad referencial en cada subtipo sin duplicar columnas, y porque permite que tablas como ENCUENTRO o COMPRA referencien roles específicos (ADMINISTRADOR, USUARIO_GENERAL) sin ambigüedad. Esta decisión impactó directamente en la complejidad de las consultas de autenticación, que requieren verificar en qué tabla de herencia existe el email autenticado.

El mecanismo de QR dinámico resultó ser el requerimiento más interesante desde el punto de vista de seguridad. La solución implementada — un token UUID regenerado automáticamente cada 30 segundos — cumple el objetivo de impedir el fraude por captura de pantalla sin necesidad de infraestructura adicional (cámaras, lectores físicos), lo cual es apropiado para el alcance de un prototipo académico.

## Decisiones de implementación destacadas

- **Comisión almacenada por transacción:** la tasa del 5% se guarda en cada compra en lugar de calcularse a demanda, anticipando que la tasa puede cambiar a lo largo del tiempo sin afectar el histórico.
- **Estado `transferida_pendiente`:** al iniciar una transferencia la entrada queda bloqueada, impidiendo operaciones concurrentes sobre la misma entrada durante el proceso de aceptación/rechazo.
- **Límite de 3 transferencias:** implementado como contador en la entrada, lo que permite verificarlo en O(1) sin recorrer el historial.
- **Irreversibilidad del consumo:** una entrada en estado `consumida` no puede ser modificada, garantizando la integridad del registro de auditoría.

## Próximos pasos

- **Autenticación con JWT:** reemplazar Basic Auth por tokens JWT para mejorar la seguridad y eliminar el envío de credenciales en cada request.
- **QR visual escaneble:** integrar una librería de generación de QR (como `qrcode.js`) para mostrar una imagen escaneable en lugar del token en texto, y adaptar el proceso de validación para usar la cámara del dispositivo del funcionario.
- **Paginación y búsqueda:** los listados de entradas, compras y transferencias no están paginados, lo que podría ser un problema de performance con volumen real de datos.
- **Índices de performance:** agregar índices sobre `ENTRADA.propietario_actual`, `ENTRADA.id_encuentro`, `TRANSFERENCIA.usuario_emisor` y `TRANSFERENCIA.usuario_receptor` para acelerar las consultas más frecuentes.
- **Notificaciones:** implementar notificaciones en tiempo real (WebSocket o Server-Sent Events) para que el receptor de una transferencia sea alertado sin necesidad de refrescar la página.
- **Módulo de estadísticas para administrador:** expandir los rankings actuales con filtros por fecha, país y estadio, y exportación a CSV.
