# ===========
# LOCAL (solo DB para IntelliJ)
# ===========
local-db:
	docker compose -f docker-compose.local.yml up

local-db-stop:
	docker compose -f docker-compose.local.yml down

local-db-clean:
	docker compose -f docker-compose.local.yml down -v --remove-orphans

# ===========
# DEVELOPMENT
# ===========
dev:
	docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build

dev-up:
	docker compose -f docker-compose.yml -f docker-compose.dev.yml up

dev-restart:
	docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build --force-recreate

dev-stop:
	docker compose -f docker-compose.yml -f docker-compose.dev.yml stop

dev-down:
	docker compose -f docker-compose.yml -f docker-compose.dev.yml down

dev-clean:
	docker compose -f docker-compose.yml -f docker-compose.dev.yml down -v --remove-orphans

# ==========
# PRODUCTION
# ==========
prod:
	docker compose -f docker-compose.yml -f docker-compose.prod.yml up --build

prod-up:
	docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d

prod-down:
	docker compose -f docker-compose.yml -f docker-compose.prod.yml down

prod-clean:
	docker compose -f docker-compose.yml -f docker-compose.prod.yml down -v --remove-orphans

# =======
# UTILITY
# =======
status:
	docker ps

logs-api:
	docker logs -f community-help-api

logs-db:
	docker logs -f community-help-db

logs-adminer:
	docker logs -f community-help-adminer

shell-api:
	docker exec -it community-help-api sh

shell-db:
	docker exec -it community-help-db sh