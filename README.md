# Community Help

**🚧 Proyecto en desarrollo 🚧**

**Community Help** es una plataforma local de solidaridad vecinal que conecta a personas que quieren **donar bienes** (alimentos, ropa, medicamentos, muebles, etc.) con quienes los necesitan, y también permite **solicitar ayuda puntual** (compra, transporte, recogida, compañía, etc.) a voluntarios cercanos.

---

## Tecnologías

### Backend
- **Java 21** + **Spring Boot 4**
- **PostgreSQL 18** + **PostGIS** — geolocalización y búsquedas por proximidad
- **Flyway** — migraciones versionadas de base de datos
- **Hibernate Spatial** — integración JPA con tipos geométricos
- **Spring Security** + **JWT** — autenticación stateless
- **OAuth2 / Google Login** — autenticación social con Google
- **WebSocket / STOMP** — mensajería en tiempo real
- **MapStruct** — mapeo de DTOs
- **Docker** — contenedores para despliegue
- **Thymeleaf** — plantillas HTML para emails transaccionales
- **Brevo (SMTP)** — envío de emails
- **OpenRouteService** — cálculo de rutas y tiempos de viaje reales
- **Spring Cache** — caché de tiempos de viaje para optimizar llamadas a la API
- **Bucket4j** — rate limiting en endpoints de autenticación

### Frontend
- **React 18** + **TypeScript** + **Vite**
- **Bootstrap 5** — estilos y componentes UI
- **React Router v6** — enrutamiento
- **Axios** — cliente HTTP
- **React Leaflet** — mapas interactivos con OpenStreetMap
- **@stomp/stompjs** — cliente WebSocket STOMP para el chat en tiempo real
- **React Toastify** — notificaciones

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
- **Login con Google** — OAuth2 integrado en backend y frontend.
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

## Migraciones de base de datos (Flyway)

El proyecto utiliza **Flyway** para gestionar las migraciones de la base de datos de forma versionada y reproducible.

### Convención de nombres

- Formato: `V{version}__{description}.sql`
- La versión debe ser secuencial (ej: `V1`, `V2`, `V10`)

### Verificar estado de migraciones

```sql
-- Consultar el historial de migraciones aplicadas
SELECT * FROM flyway_schema_history;
```

---

## API

La documentación interactiva de la API está disponible en:
```
http://localhost:8080/swagger-ui.html
```

Para probar los endpoints autenticados:

1. Regístrate con `POST /api/v1/auth/register`
2. Verifica tu email con `POST /api/v1/auth/verify-email`
3. Obtén tu token con `POST /api/v1/auth/login`
4. Haz clic en **Authorize** e introduce `Bearer <token>`

La especificación OpenAPI en JSON está disponible en `/v3/api-docs`.

El plan de pruebas completo está disponible en [Google Sheets](https://docs.google.com/spreadsheets/d/1We3w7b18pmL2A87966vYyekEPzlfY47H3h4I0qm116g/edit?usp=sharing).

---

## Configuración y arranque

### Requisitos

- Java 21
- Node.js 20+
- Docker
- Cuenta en [Brevo](https://app.brevo.com) para el envío de emails
- Cuenta en [OpenRouteService](https://openrouteservice.org) para el cálculo de rutas
- Credenciales OAuth2 de Google en [Google Cloud Console](https://console.cloud.google.com)

### Variables de entorno

Copia `.env.example` y rellena tus valores:
```bash
cp .env.example .env
```

El archivo `.env` debe estar en la raíz de `community-help-pro/`.

```env
# PostgreSQL
POSTGRES_DB=community_help_db
POSTGRES_USER=your_postgres_user
POSTGRES_PASSWORD=your_postgres_password

# API Port
API_PORT=8080

# JWT
JWT_SECRET=your_jwt_secret_minimum_32_characters_long
JWT_EXPIRATION_MS=3600000
JWT_EXPIRED_IN=3600000

# Admin inicial
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=your_admin_password

# OpenRoute Service API
OPENROUTE_API_KEY=your_openroute_api_key

# Email (Brevo SMTP)
MAIL_HOST=smtp-relay.brevo.com
MAIL_PORT=587
MAIL_USERNAME=your_brevo_smtp_user
MAIL_PASSWORD=your_brevo_smtp_password
MAIL_FROM=your_verified_sender@example.com

# Google OAuth2
OAUTH2_CLIENT_ID=your_google_client_id
OAUTH2_CLIENT_SECRET=your_google_client_secret
OAUTH2_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google

# Frontend URLs
URL_FRONTEND=http://localhost:5173
URL_FRONTEND_LOGIN=http://localhost:5173/login
URL_FRONTEND_OAUTH2_SUCCESS=http://localhost:5173/oauth2/callback
```

> **Nota sobre Brevo:** las claves SMTP expiran tras 90 días de inactividad. Genera una nueva desde `Settings > SMTP & API`.

> **Nota sobre OAuth2:** registra `http://localhost:8080/login/oauth2/code/google` como URI de redirección autorizada en Google Cloud Console.

> **Nota sobre OpenRouteService:** el plan gratuito permite 2.000 peticiones diarias, suficiente para desarrollo.

### Arranque del backend

```bash
# Levanta PostgreSQL y Adminer
docker compose up -d db adminer

# Arranca la API
./mvnw spring-boot:run
```

Adminer estará disponible en `http://localhost:8888` — selecciona PostgreSQL, servidor `db`.

### Arranque del frontend

```bash
cd community-help-web
npm install
npm run dev
```

El frontend estará disponible en `http://localhost:5173`.

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

El digest se envía cada 5 minutos en dev. Para probar inmediatamente:
```properties
notification.digest.interval-ms=30000
```

```sql
SELECT volunteer_email, entity_title, entity_type, sent, created_at
FROM pending_notifications
ORDER BY created_at DESC;
```

### Coordenadas de prueba

Para que el motor de matching funcione correctamente, usa coordenadas distintas para cada usuario de prueba.

| Usuario | Latitud | Longitud |
|---|---|---|
| Usuario A | 43.5322 | -5.6611 |
| Usuario B | 43.5330 | -5.6620 |
| Usuario C | 43.5350 | -5.6650 |
| Donación / Solicitud | 43.5325 | -5.6615 |

---

## Estructura del proyecto
```
community-help-pro/
├── community-help-api/          # Backend Spring Boot
│   └── src/main/java/com/communityhelp/app/
│       ├── auth/                # JWT, OAuth2, verificación email, recuperación contraseña
│       ├── chat/                # Mensajería REST y WebSocket/STOMP
│       ├── common/              # Location, excepciones, OpenRoute, persistencia base
│       ├── config/              # Seguridad, CORS, WebSocket, caché, scheduler
│       ├── donation/            # Donaciones, matching, ciclo de vida
│       ├── email/               # Emails con plantillas Thymeleaf
│       ├── helprequest/         # Solicitudes de ayuda, matching, ciclo de vida
│       ├── notification/        # Digest de proposals, limpieza
│       ├── otp/                 # Códigos OTP
│       ├── proposal/            # Motor de matching y scoring
│       ├── review/              # Reseñas y rating
│       ├── security/            # Filtros JWT, rate limiting
│       ├── user/                # Gestión de usuarios
│       └── volunteer/           # Perfil, habilidades, transporte
│
└── community-help-web/          # Frontend React + TypeScript
└── src/
├── components/          # Menubar, mapas, OTP input
├── context/             # Estado global (auth, token, usuario)
├── hooks/               # useAppContext, useGeolocation, useChat
├── pages/               # Auth, Home, solicitudes, donaciones, chat, perfil
└── types/               # Interfaces TypeScript del dominio
```

---

## Perfiles

| Perfil | Uso | Radio inicial | Retry | Retención notificaciones |
|---|---|---|---|---|
| `dev` | Local | 5 km | 10 min | 7 días |
| `prod` | Producción | 10 km | 30 min | 30 días |
