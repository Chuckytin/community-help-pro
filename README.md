# Community Help

**🚧 Proyecto en desarrollo 🚧**

**Community Help** es una plataforma local de solidaridad vecinal que conecta a personas que quieren **donar bienes** (alimentos, ropa, medicamentos, muebles, etc.) con quienes los necesitan, y también permite **solicitar ayuda puntual** (compra, transporte, recogida, compañía, etc.) a voluntarios cercanos.

---

## Tecnologías

- **Java 21** + **Spring Boot 4**
- **PostgreSQL 18** + **PostGIS** — geolocalización y búsquedas por proximidad
- **Hibernate Spatial** — integración JPA con tipos geométricos
- **Spring Security** + **JWT** — autenticación stateless
- **WebSocket / STOMP** — mensajería en tiempo real
- **MapStruct** — mapeo de DTOs
- **Docker** — contenedores para la base de datos
- **Thymeleaf** — plantillas HTML para emails transaccionales
- **Brevo (SMTP)** — envío de emails
- **OpenRouteService** — cálculo de rutas y tiempos de viaje reales
- **Spring Cache** — caché de tiempos de viaje para optimizar llamadas a la API
- **Bucket4j** — rate limiting en endpoints de autenticación

---

## Funcionalidades principales

- Publicar **donaciones** con ubicación, caducidad, tipo, cantidad y descripción. No se permite duplicar el título de una donación activa.
- Crear **solicitudes de ayuda** (Help Requests) con título, descripción, fecha límite y radio de acción. No se permite duplicar el título de una solicitud abierta.
- **Búsqueda por proximidad** de donaciones y solicitudes cercanas, filtrable por tipo y radio en metros.
- **Motor de matching automático** que conecta voluntarios cercanos con donaciones y solicitudes compatibles, evaluando tiempo de viaje real, habilidades, rating y carga de trabajo. El radio de búsqueda se amplía progresivamente si no hay respuesta.
- **Filtro de viabilidad por deadline** — el motor descarta voluntarios que no pueden llegar a tiempo antes de la fecha límite, usando tiempos de viaje reales calculados en paralelo.
- **Estimación de tiempo de viaje** para el voluntario al consultar una tarea, usando su modo de transporte configurado y mostrando también la opción más rápida disponible.
- **Notificaciones por email a voluntarios** cuando reciben nuevas proposals, agrupadas en un digest periódico para evitar spam. Configurable por voluntario.
- **Chat privado** entre solicitante y voluntario para coordinar detalles, con soporte WebSocket para mensajería en tiempo real.
- Sistema de **reseñas y puntuaciones** entre participantes tras completar una interacción.
- **Sistema de autenticación con verificación de email** — OTP por correo al registrarse, con recuperación de contraseña. Las cuentas no verificadas se eliminan automáticamente pasadas 24 horas.
- **Rate limiting** en endpoints de autenticación para proteger contra fuerza bruta y abuso.
- **Limpieza automática de datos** — cuentas sin verificar y notificaciones enviadas antiguas se purgan periódicamente para mantener la base de datos sana.
- **API documentada con Swagger / OpenAPI** accesible en `/swagger-ui.html`.

---

## Motor de matching

El sistema genera proposals automáticas mediante un motor de scoring configurable que evalúa:

| Factor | Peso |
|---|---|
| Tiempo de viaje real al voluntario | 50% |
| Coincidencia de habilidades | 35% |
| Rating del voluntario | 10% |
| Carga de trabajo activa | 5% |

El factor de distancia utiliza el **tiempo de viaje real** calculado por OpenRouteService según el modo de transporte del voluntario (a pie, en bici o en coche), en lugar de la distancia en línea recta. Esto permite priorizar al voluntario que llega antes, independientemente de la distancia geométrica.

Los tiempos de viaje se calculan **en paralelo** para todos los candidatos y se **cachean** para evitar llamadas repetidas a la API cuando múltiples entidades comparten voluntarios en la misma zona.

Si ningún voluntario acepta una proposal en el tiempo configurado, el sistema reintenta automáticamente ampliando el radio de búsqueda de forma progresiva hasta un máximo configurable. Los parámetros son completamente ajustables por entorno vía `application.properties`.

---

## Modos de transporte

Los voluntarios pueden configurar su modo de transporte habitual en su perfil:

| Modo | Valor |
|---|---|
| A pie | `FOOT_WALKING` |
| Bicicleta | `CYCLING_REGULAR` |
| Coche | `DRIVING_CAR` |

Al consultar una donación o solicitud de ayuda, la respuesta incluye la estimación de tiempo según el modo del voluntario y cuál sería la opción más rápida disponible.

---

## Rate limiting

Los siguientes endpoints de autenticación tienen límite de peticiones por IP para proteger contra abuso:

| Endpoint | Límite |
|---|---|
| `POST /api/v1/auth/register` | 3 req/min |
| `POST /api/v1/auth/forgot-password` | 3 req/min |
| `POST /api/v1/auth/verify-email` | 10 req/min |
| `POST /api/v1/auth/reset-password` | 5 req/min |

Al superar el límite se devuelve `429 Too Many Requests`.

---

## API

La documentación interactiva de la API está disponible en:
```
http://localhost:8080/swagger-ui.html
```

Para probar los endpoints autenticados:

1. Regístrate con `POST /api/v1/auth/register`
2. Verifica tu email con `POST /api/v1/auth/verify-email` (revisa tu bandeja de entrada)
3. Obtén tu token con `POST /api/v1/auth/login`
4. Haz clic en **Authorize** (icono de candado) e introduce `Bearer <token>`

La especificación OpenAPI en JSON está disponible en `/v3/api-docs`.

El plan de pruebas completo con los casos de prueba organizados por módulo está disponible en [Google Sheets](https://docs.google.com/spreadsheets/d/1We3w7b18pmL2A87966vYyekEPzlfY47H3h4I0qm116g/edit?usp=sharing).

---

## Configuración y arranque

### Requisitos

- Java 21
- Docker
- Cuenta en [Brevo](https://app.brevo.com) para el envío de emails (plan gratuito suficiente)
- Cuenta en [OpenRouteService](https://openrouteservice.org) para el cálculo de rutas (plan gratuito suficiente)

### Variables de entorno

Copia `.env.example` y rellena tus valores:
```bash
cp .env.example .env
```

El archivo `.env` debe estar en la raíz de `community-help-api/`. Spring lo carga automáticamente en el perfil `dev` — no es necesario configurar nada en el IDE.
```env
# PostgreSQL (Docker)
POSTGRES_DB=community_help_db
POSTGRES_USER=your_postgres_user
POSTGRES_PASSWORD=your_postgres_password

# Spring datasource (app)
SPRING_DATASOURCE_DB=community_help_db
SPRING_DATASOURCE_USERNAME=your_postgres_user
SPRING_DATASOURCE_PASSWORD=your_postgres_password

# JWT
JWT_SECRET=your_jwt_secret_minimum_32_characters_long
JWT_EXPIRATION_MS=3600000
JWT_EXPIRED_IN=3600000

# Admin inicial
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=your_admin_password

# OpenRoute Service API — https://openrouteservice.org
OPENROUTE_API_KEY=your_openroute_api_key

# Email (Brevo SMTP — https://app.brevo.com)
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=your_brevo_smtp_user
MAIL_PASSWORD=your_brevo_smtp_password
MAIL_FROM=your_verified_sender@example.com

# Frontend URLs
URL_FRONTEND=your_frontend_url
URL_FRONTEND_LOGIN=your_frontend_login_url
```

> **Nota sobre Brevo:** las claves SMTP de Brevo expiran tras 90 días de inactividad. Si llevas tiempo sin usar el proyecto, genera una nueva clave desde `Settings > SMTP & API` en tu cuenta de Brevo.

> **Nota sobre OpenRouteService:** el plan gratuito permite 2.000 peticiones diarias y 40 por minuto, suficiente para desarrollo. En producción con muchos voluntarios activos considera el plan de pago o implementar un caché más agresivo.

### Arranque

1. Levanta la base de datos:
```bash
docker compose up -d db
```

2. Arranca la aplicación:
```bash
./mvnw spring-boot:run
```

La base de datos se inicializa automáticamente con PostGIS y el índice espacial:
```sql
-- docker-config/postgres-init.sql
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE INDEX IF NOT EXISTS idx_users_location_gist ON users USING GIST(location);
```

---

## Pruebas y desarrollo

### Verificación de email

Al registrarse, el sistema envía un OTP al email del usuario. Si durante las pruebas usas emails inventados o no tienes acceso al buzón, puedes verificar las cuentas manualmente:
```sql
-- Verificar todos los usuarios de una vez
UPDATE users SET email_verified = true;

-- O verificar un usuario concreto
UPDATE users SET email_verified = true WHERE email = 'usuario@ejemplo.com';
```

También puedes consultar el OTP generado directamente en la tabla para probar el flujo completo sin necesitar el email:
```sql
SELECT email, code, type, expires_at, used FROM otp_codes ORDER BY expires_at DESC;
```

> Las cuentas no verificadas se eliminan automáticamente a las 3:00 AM si llevan más de 24 horas sin verificar.

### Notificaciones de proposals

El sistema agrupa las notificaciones de nuevas proposals y envía un digest periódico (cada 5 minutos en dev). Si quieres probar el envío inmediatamente, puedes reducir el intervalo temporalmente:
```properties
# application.properties
notification.digest.interval-ms=30000
```

Para consultar las notificaciones pendientes de enviar:
```sql
SELECT volunteer_email, entity_title, entity_type, sent, created_at
FROM pending_notifications
ORDER BY created_at DESC;
```

> Las notificaciones ya enviadas se purgan automáticamente a las 3:30 AM tras 30 días de retención (7 días en dev). Esto evita que la tabla crezca indefinidamente sin perder la capacidad de deduplicación.

### Idempotencia de donaciones y solicitudes

El sistema bloquea la creación de una donación o solicitud de ayuda si el usuario ya tiene una activa con el mismo título. Si intentas crear un duplicado recibirás un `400 Bad Request` con el mensaje de error correspondiente.

### Rate limiting

Durante las pruebas con Swagger, si realizas más peticiones de las permitidas en un endpoint de auth recibirás un `429 Too Many Requests`. Espera un minuto para que el bucket se recargue o reinicia la aplicación para limpiar el estado en memoria.

### Coordenadas de prueba

Para que el motor de matching funcione correctamente, registra los usuarios de prueba con coordenadas distintas entre sí y distintas a las de las donaciones/solicitudes que crees. Usar las mismas coordenadas para todos produce `distance 0m / travel 0s` en los logs, lo cual es un artefacto de pruebas y no refleja el comportamiento real en producción.

Coordenadas de ejemplo en Gijón:

| Usuario | Latitud | Longitud |
|---|---|---|
| Usuario A | 43.5322 | -5.6611 |
| Usuario B | 43.5330 | -5.6620 |
| Usuario C | 43.5350 | -5.6650 |
| Donación / Solicitud | 43.5325 | -5.6615 |

---

## Estructura del proyecto
```
community-help-api/
├── auth/           # Autenticación JWT, verificación de email y recuperación de contraseña
├── chat/           # Mensajería REST y WebSocket
├── common/         # Location, excepciones, OpenRoute (rutas y tiempos de viaje), persistencia base
├── config/         # Seguridad, OpenAPI, caché, scheduler, inicialización y limpieza periódica
├── donation/       # Donaciones, búsqueda por proximidad, validación de duplicados y ciclo de vida
├── email/          # Servicio de envío de emails con plantillas Thymeleaf
├── helprequest/    # Solicitudes de ayuda, búsqueda por proximidad, validación de duplicados y ciclo de vida
├── notification/   # Digest de notificaciones de proposals para voluntarios y limpieza de registros
├── otp/            # Generación y validación de códigos OTP
├── proposal/       # Motor de matching, scoring, filtro de viabilidad y gestión de proposals
├── review/         # Reseñas y recálculo de rating
├── security/       # Filtros JWT, rate limiting y configuración de Spring Security
├── user/           # Gestión de usuarios y limpieza de cuentas no verificadas
└── volunteer/      # Perfil, habilidades, modo de transporte y preferencias de notificación
```

---

## Perfiles

| Perfil | Uso | Radio inicial | Retry | Retención notificaciones |
|---|---|---|---|---|
| `dev` | Local | 5 km | 10 min | 7 días |
| `prod` | Producción | 10 km | 30 min | 30 días |
