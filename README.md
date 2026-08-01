# Community Help API

**🚧 Proyecto en desarrollo 🚧**

**Community Help** es una plataforma local de solidaridad vecinal que conecta a personas que quieren **donar bienes** (alimentos, ropa, medicamentos, muebles, etc.) con quienes los necesitan, y también permite **solicitar ayuda puntual** (compra, transporte, recogida, compañía, etc.) a voluntarios cercanos.

Este repositorio contiene el **backend** (API REST). El frontend (React + TypeScript) vive en un proyecto hermano, `community-help-web`, actualmente en desarrollo y fuera del alcance de este README.

---

## Tecnologías

- **Java 21** + **Spring Boot 4**
- **PostgreSQL 18** + **PostGIS** — geolocalización y búsquedas por proximidad
- **Flyway** — migraciones versionadas de base de datos
- **Hibernate Spatial** — integración JPA con tipos geométricos
- **Spring Security** + **JWT** — autenticación stateless
- **OAuth2 / Google Login** — autenticación social con Google
- **WebSocket / STOMP** — mensajería en tiempo real
- **MapStruct** — mapeo de DTOs
- **Docker** + **Docker Compose** + **Makefile** — entornos `local`, `dev` y `prod`
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
- **Login con Google (OAuth2)** — con reactivación automática de cuentas dadas de baja (soft delete) que vuelven a autenticarse, y redirección al frontend tanto en éxito como en fallo del flujo.
- **Rate limiting** en endpoints de autenticación para proteger contra fuerza bruta y abuso.
- **Limpieza automática de datos** — cuentas sin verificar y notificaciones enviadas antiguas se purgan periódicamente para mantener la base de datos sana.
- **Soft delete de usuarios** — las cuentas eliminadas por el propio usuario quedan marcadas como inactivas (`active = false`) y excluidas de login y búsquedas, sin perder el histórico de donaciones, solicitudes y reviews asociado.
- **API documentada con Swagger / OpenAPI** accesible en `/swagger-ui.html`.
- **Datos de prueba (seed)** — usuarios, voluntarios, donaciones y solicitudes de ejemplo, cargados automáticamente en `local`/`dev`, disparando el motor de matching real desde el primer arranque.

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

Si ningún voluntario acepta una proposal en el tiempo configurado, el sistema reintenta automáticamente ampliando el radio de búsqueda de forma progresiva hasta un máximo configurable. Los parámetros son completamente ajustables por entorno vía `application-{profile}.yml`.

El motor se dispara de forma reactiva mediante eventos de dominio (`DonationCreatedEvent`, `HelpRequestCreatedEvent`, `VolunteerUpdatedEvent`, etc.), procesados de forma asíncrona tras el commit de la transacción correspondiente — tanto en el flujo normal de la API como en los seeders de datos de prueba.

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

## Migraciones de base de datos (Flyway)

El proyecto utiliza **Flyway** para gestionar las migraciones de la base de datos de forma versionada y reproducible. Está activo en el perfil `prod`; en `local` y `dev` el esquema se genera vía Hibernate (`ddl-auto`).

### Convención de nombres

- Formato: `V{version}__{description}.sql`
- La versión debe ser secuencial (ej: `V1`, `V2`, `V10`)

### Migraciones actuales

| Versión | Descripción |
|---|---|
| `V1` | Esquema inicial |
| `V2` | Extensión PostGIS + índices espaciales y de consulta frecuente |
| `V3` | Permite `password_hash` nulo para usuarios OAuth2 |
| `V4` | Índice sobre `volunteer_skills.volunteer_id` |

### Verificar estado de migraciones

```sql
-- Consultar el historial de migraciones aplicadas
SELECT * FROM flyway_schema_history;
```

---

## API

La documentación interactiva de la API está disponible en (perfiles `local`/`dev`):

```
http://localhost:8080/swagger-ui.html
```

Para probar los endpoints autenticados:

1. Regístrate con `POST /api/v1/auth/register`
2. Verifica tu email con `POST /api/v1/auth/verify-email`
3. Obtén tu token con `POST /api/v1/auth/login`
4. Haz clic en **Authorize** e introduce `Bearer <token>`

También puedes iniciar sesión con Google en `/oauth2/authorization/google`; el backend redirige al frontend con el JWT como query param en caso de éxito, o con un parámetro de error en caso de fallo.

La especificación OpenAPI en JSON está disponible en `/v3/api-docs`.

El plan de pruebas completo está disponible en [Google Sheets](https://docs.google.com/spreadsheets/d/1We3w7b18pmL2A87966vYyekEPzlfY47H3h4I0qm116g/edit?usp=sharing).

---

## Configuración y arranque

### Requisitos

- Java 21
- Docker + Docker Compose
- `make` — en Windows: `choco install make`
- Cuenta en [Brevo](https://app.brevo.com) para el envío de emails
- Cuenta en [OpenRouteService](https://openrouteservice.org) para el cálculo de rutas
- Credenciales OAuth2 de Google en [Google Cloud Console](https://console.cloud.google.com)

### Variables de entorno

El proyecto usa dos archivos de entorno según el perfil: `.env.dev` (usado también en `local`) y `.env.prod`, ninguno de los dos versionado en git.

```bash
cp .env.dev.example .env.dev
cp .env.prod.example .env.prod
```

Ambos archivos deben estar en la raíz de `community-help-api/`, junto al `pom.xml`.

`.env.dev.example`:
```env
# PostgreSQL
POSTGRES_HOST=db
POSTGRES_PORT=5432
POSTGRES_DB=community_help_db
POSTGRES_USER=your_postgres_user
POSTGRES_PASSWORD=your_postgres_password

# API
API_PORT=8080

# JWT
JWT_SECRET=devSuperSecretKeyForJWT1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ
JWT_EXPIRATION_MS=3600000
JWT_EXPIRED_IN=3600000

# Admin inicial
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=your_admin_password

# OpenRouteService
OPENROUTE_API_KEY=your_openroute_api_key

# Email (Brevo SMTP)
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=your_brevo_smtp_user
MAIL_PASSWORD=your_brevo_smtp_password
MAIL_FROM=your_verified_sender@example.com

# Google OAuth2
OAUTH2_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google
OAUTH2_CLIENT_ID=your_google_client_id
OAUTH2_CLIENT_SECRET=your_google_client_secret

# Frontend URLs
URL_FRONTEND=http://localhost:5173
URL_FRONTEND_LOGIN=http://localhost:5173/login
URL_FRONTEND_OAUTH2_SUCCESS=http://localhost:5173/oauth2/callback
```

`.env.prod.example` sigue la misma estructura, añadiendo `DB_SSL_MODE=disable` y apuntando las URLs de frontend/OAuth2 al dominio real. Ver el archivo para el detalle completo.

> **Nota sobre Brevo:** las claves SMTP expiran tras 90 días de inactividad. Genera una nueva desde `Settings > SMTP & API`.

> **Nota sobre OAuth2:** registra `http://localhost:8080/login/oauth2/code/google` (o el dominio de prod correspondiente) como URI de redirección autorizada en Google Cloud Console.

> **Nota sobre OpenRouteService:** el plan gratuito permite 2.000 peticiones diarias, suficiente para desarrollo.

### Arranque con Docker + Makefile

El proyecto usa tres flujos de trabajo, cada uno con su propio `docker-compose` y perfil de Spring:

| Entorno | Uso | `docker-compose` | Perfil Spring |
|---|---|---|---|
| `local` | Solo levanta la BD, la API corre desde el IDE (IntelliJ) | `docker-compose.local.yml` | `local` |
| `dev` | API + BD dockerizadas, hot-reload con Maven | `docker-compose.yml` + `docker-compose.dev.yml` | `dev` |
| `prod` | API + BD dockerizadas, build multi-stage optimizado | `docker-compose.yml` + `docker-compose.prod.yml` | `prod` |

```bash
# LOCAL — solo levanta PostgreSQL + Adminer, la API se arranca desde el IDE
make local-db

# DEV — levanta API + BD con build (usar al empezar o tras cambios en código)
make dev

# PROD — levanta API + BD con build multi-stage
make prod
```

Para la mayoría de casos en desarrollo, basta con `make dev-down` + `make dev`.

Adminer estará disponible en `http://localhost:8888` (perfiles `local` y `dev`) — selecciona PostgreSQL, servidor `db`.

### Comandos disponibles (`Makefile`)

**Local (solo BD, API desde el IDE)**

| Comando | Descripción |
|---|---|
| `make local-db` | Levanta solo la base de datos |
| `make local-db-stop` | Para los contenedores conservando estado |
| `make local-db-clean` | Reset total — borra volúmenes y base de datos |

**Desarrollo**

| Comando | Descripción |
|---|---|
| `make dev` | Levanta dev con build |
| `make dev-up` | Levanta dev sin rebuild |
| `make dev-restart` | Fuerza recreación sin hacer down |
| `make dev-stop` | Para contenedores conservando estado |
| `make dev-down` | Elimina contenedores sin borrar datos |
| `make dev-clean` | Reset total — borra volúmenes y base de datos |

**Producción**

| Comando | Descripción |
|---|---|
| `make prod` | Levanta prod con build |
| `make prod-up` | Levanta prod sin rebuild |
| `make prod-down` | Para prod conservando datos |
| `make prod-clean` | Reset total prod — borra volúmenes y base de datos |

**Utilidades**

| Comando | Descripción |
|---|---|
| `make status` | Estado de todos los contenedores activos |
| `make logs-api` | Logs en tiempo real del contenedor de la API |
| `make logs-db` | Logs en tiempo real de Postgres |
| `make logs-adminer` | Logs en tiempo real de Adminer |
| `make shell-api` | Shell del contenedor de la API |
| `make shell-db` | Shell del contenedor Postgres |

### Arranque sin Docker (Maven directo)

```bash
# Copia y rellena las variables de entorno si vas a usar el perfil dev
cp .env.dev.example .env.dev

# Arranca con el perfil local (BD ya levantada con `make local-db`)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

## Datos de prueba (seed)

En los perfiles `local` y `dev`, al arrancar la aplicación se cargan automáticamente datos de ejemplo desde `src/main/resources/seed/`:

| Seeder | Origen | Contenido |
|---|---|---|
| `UserSeeder` | `seed/users.json` | Usuarios de ejemplo, algunos con perfil de voluntario (skills, radio, modo de transporte) |
| `DonationSeeder` | `seed/donations.json` | Donaciones asociadas a los usuarios sembrados |
| `HelpRequestSeeder` | `seed/helprequests.json` | Solicitudes de ayuda asociadas a los usuarios sembrados |

Cada seeder es idempotente (no duplica datos si ya existen) y se ejecuta en orden (`AdminSeeder` → `UserSeeder` → `DonationSeeder` → `HelpRequestSeeder`). Todos los usuarios de seed se crean con `emailVerified = true` y `active = true`, listos para iniciar sesión sin pasar por el flujo OTP.

`DonationSeeder` y `HelpRequestSeeder` publican los mismos eventos de dominio que el flujo normal de la API (`DonationCreatedEvent`, `HelpRequestCreatedEvent`), por lo que el motor de matching genera proposals reales automáticamente unos segundos después del arranque.

Las contraseñas de los usuarios de seed están en texto plano dentro de los propios JSON (p. ej. `password123`), pensadas únicamente para entornos `local`/`dev`.

---

## Pruebas y desarrollo

### Verificación de email

```sql
-- Verificar todos los usuarios de una vez
UPDATE users SET email_verified = true;

-- O uno concreto
UPDATE users SET email_verified = true WHERE email = 'usuario@ejemplo.com';
```

También puedes consultar el OTP directamente:
```sql
SELECT email, code, type, expires_at, used FROM otp_codes ORDER BY expires_at DESC;
```

> Las cuentas no verificadas se eliminan a las 3:00 AM si llevan más de 24 horas sin verificar.

### Notificaciones de proposals

El digest se envía cada 5 minutos en `dev`/`local`. Para probar inmediatamente, edita el perfil correspondiente:
```yaml
notification:
  digest:
    interval-ms: 30000
```

```sql
SELECT volunteer_email, entity_title, entity_type, sent, created_at
FROM pending_notifications
ORDER BY created_at DESC;
```

### Coordenadas de prueba

Para que el motor de matching funcione correctamente, usa coordenadas distintas para cada usuario de prueba. Los usuarios del seed ya siguen este criterio; si añades usuarios manualmente, usa como referencia:

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
├── .env.dev.example
├── .env.prod.example
├── .gitignore
├── .dockerignore
├── docker-compose.yml
├── docker-compose.local.yml
├── docker-compose.dev.yml
├── docker-compose.prod.yml
├── Dockerfile.dev
├── Dockerfile.prod
├── Makefile
├── pom.xml
└── src/main/
├── resources/
│ ├── db/migration/ # V1-V4, migraciones Flyway
│ └── seed/ # users.json, donations.json, helprequests.json
└── java/com/communityhelp/app/
├── auth/ # JWT (JwtService), OAuth2 (success/failure handlers),
│ # verificación email, recuperación contraseña
├── chat/ # Mensajería REST y WebSocket/STOMP
├── common/ # Location, excepciones base, OpenRoute, persistencia base
├── config/ # Seguridad, CORS, WebSocket, caché, scheduler
│ └── seed/ # UserSeeder, DonationSeeder, HelpRequestSeeder
├── donation/ # Donaciones, matching, ciclo de vida, excepciones propias
├── email/ # Emails con plantillas Thymeleaf
├── helprequest/ # Solicitudes de ayuda, matching, ciclo de vida, excepciones propias
├── notification/ # Digest de proposals, limpieza
├── otp/ # Códigos OTP
├── proposal/ # Motor de matching y scoring
├── review/ # Reseñas y rating
├── security/ # Filtros JWT, rate limiting
├── user/ # Gestión de usuarios, excepciones propias (email duplicado, no verificado)
└── volunteer/ # Perfil, habilidades, transporte
```

---

## Perfiles

| Perfil | Uso | BD | Radio inicial | Retry | Retención notificaciones | Seed |
|---|---|---|---|---|---|---|
| `local` | Desarrollo desde el IDE, solo BD en Docker | Local (root/password) | 5 km | 2 min | 7 días | Sí |
| `dev` | Desarrollo con API + BD dockerizadas | Docker (`.env.dev`) | 5 km | 2 min | 7 días | Sí |
| `prod` | Producción | Docker (`.env.prod`) | 10 km | 30 min | 30 días | No |
