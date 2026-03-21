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
2. Obtén tu token con `POST /api/v1/auth/login`
3. Haz clic en **Authorize** (icono de candado) e introduce `Bearer <token>`

La especificación OpenAPI en JSON está disponible en `/v3/api-docs`.

---

## Configuración y arranque

### Requisitos

- Java 21
- Docker

### Variables de entorno

Crea un archivo `.env` en la raíz del proyecto con las siguientes variables:
```env
SPRING_DATASOURCE_DB=community_help_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=yourpassword
JWT_SECRET=your_jwt_secret
JWT_EXPIRATION_MS=86400000
JWT_EXPIRED_IN=86400000
ADMIN_EMAIL=admin@communityhelp.com
ADMIN_PASSWORD=adminpassword
OPENROUTE_API_KEY=your_openroute_key
```

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

## Estructura del proyecto
```
community-help-api/
├── auth/           # Autenticación JWT
├── chat/           # Mensajería REST y WebSocket
├── common/         # Auditoría, excepciones, persistencia base
├── config/         # Seguridad, OpenAPI, inicialización
├── donation/       # Donaciones y su ciclo de vida
├── helprequest/    # Solicitudes de ayuda y su ciclo de vida
├── proposal/       # Motor de matching, scoring y gestión de proposals
├── review/         # Reseñas y recálculo de rating
├── user/           # Gestión de usuarios
└── volunteer/      # Perfil y habilidades del voluntario
```

---

## Perfiles

| Perfil | Uso | Radio inicial | Retry |
|---|---|---|---|
| `dev` | Local | 5km | 10min |
| `prod` | Producción | 10km | 30min |
