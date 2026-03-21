# Community Help

**🚧 Proyecto en desarrollo 🚧**

**Community Help** es una plataforma local de solidaridad vecinal que conecta a personas que quieren **donar bienes** (alimentos, ropa, medicamentos, muebles, etc.) con quienes los necesitan, y también permite **solicitar ayuda puntual** (compra, transporte, recogida, compañía, etc.) a voluntarios cercanos.

### Funcionalidades principales
- Publicar **donaciones** con ubicación, caducidad, tipo, cantidad y descripción.
- Crear **solicitudes de ayuda** (Help Requests) con título, descripción, fecha límite y radio de acción.
- Voluntarios se postulan para recoger donaciones o atender peticiones → se genera una **propuesta** (Proposal).
- **Chat privado** entre solicitante y voluntario para coordinar detalles y resolver dudas.
- Sistema de **reseñas y puntuaciones** para generar confianza.
- **IA integrada** que ayuda a generar propuestas automáticas entre donaciones y solicitudes compatibles (matching inteligente).
- Búsquedas geográficas cercanas (PostGIS) para mostrar solo opciones en un radio razonable.

El objetivo es hacer la ayuda comunitaria más rápida, segura y eficiente, todo de forma gratuita y 100% local.

Tecnologías clave: Spring Boot 4, Java 21, PostgreSQL + PostGIS, Docker.

### Configuración de PostgreSQL + PostGIS

Se utiliza la imagen oficial `postgis/postgis:18-3.6` que ya incluye PostGIS para la geolocalización.

1. Para levantar la base de datos con docker-compose:

```bash
docker compose up -d db
```

2. El script docker-config/postgres-init.sql activa la extensión PostGIS automáticamente y crea un índice espacial GIST sobre la columna 'location' de la tabla 'users' para optimizar las búsquedas por proximidad. al crear la base de datos:

```
-- docker-config/postgres-init.sql
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE INDEX IF NOT EXISTS idx_users_location_gist ON users USING GIST(location);
```

- Está montado en docker-compose.yml
```
volumes:
  - ./docker-config/postgres-init.sql:/docker-entrypoint-initdb.d/init-postgis.sql
```
