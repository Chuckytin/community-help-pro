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

---

## Funcionalidades principales

- Publicar **donaciones** con ubicación, caducidad, tipo, cantidad y descripción.
- Crear **solicitudes de ayuda** (Help Requests) con título, descripción, fecha límite y radio de acción.
- **Motor de matching automático** que conecta voluntarios cercanos con donaciones y solicitudes compatibles, evaluando distancia, habilidades, rating y carga de trabajo. El radio de búsqueda se amplía progresivamente si no hay respuesta.
- **Chat privado** entre solicitante y voluntario para coordinar detalles, con soporte WebSocket para mensajería en tiempo real.
- Sistema de **reseñas y puntuaciones** entre participantes tras completar una interacción.
- **Sistema de autenticación con verificación de email** — OTP por correo al registrarse, con recuperación de contraseña.
- **API documentada con Swagger / OpenAPI** accesible en `/swagger-ui.html`.

---

## Motor de matching

El sistema genera proposals automáticas mediante un motor de scoring configurable que evalúa:

| Factor | Peso |
|---|---|
| Distancia al voluntario | 50% |
| Coincidencia de habilidades | 35% |
| Rating del voluntario | 10% |
| Carga de trabajo activa | 5% |

Si ningún voluntario acepta una proposal en el tiempo configurado, el sistema reintenta automáticamente ampliando el radio de búsqueda de forma progresiva hasta un máximo configurable. Los parámetros son completamente ajustables por entorno vía `application.properties`.

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

# OpenRoute Service API
OPENROUTE_API_KEY=your_openroute_api_key

# Email (Brevo SMTP — https://app.brevo.com)
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=your_brevo_smtp_user
MAIL_PASSWORD=your_brevo_smtp_password
MAIL_FROM=your_verified_sender@example.com

# Frontend URLs (puedes usar placeholders mientras desarrollas)
URL_FRONTEND=http://localhost:5173
URL_FRONTEND_LOGIN=http://localhost:5173/login
```

> **Nota sobre Brevo:** las claves SMTP de Brevo expiran tras 90 días de inactividad. Si llevas tiempo sin usar el proyecto, genera una nueva clave desde `Settings > SMTP & API` en tu cuenta de Brevo.

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

---

## Estructura del proyecto
```
community-help-api/
├── auth/           # Autenticación JWT, verificación de email y recuperación de contraseña
├── chat/           # Mensajería REST y WebSocket
├── common/         # Location, excepciones, OpenRoute, persistencia base
├── config/         # Seguridad, OpenAPI, inicialización
├── donation/       # Donaciones y su ciclo de vida
├── email/          # Servicio de envío de emails con plantillas Thymeleaf
├── helprequest/    # Solicitudes de ayuda y su ciclo de vida
├── otp/            # Generación y validación de códigos OTP
├── proposal/       # Motor de matching, scoring y gestión de proposals
├── review/         # Reseñas y recálculo de rating
├── security/       # Filtros y configuración de Spring Security
├── user/           # Gestión de usuarios
└── volunteer/      # Perfil y habilidades del voluntario
```

---

## Perfiles

| Perfil | Uso | Radio inicial | Retry |
|---|---|---|---|
| `dev` | Local | 5 km | 10 min |
| `prod` | Producción | 10 km | 30 min |
