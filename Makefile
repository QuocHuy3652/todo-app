.PHONY: build up down restart logs ps prune

COMPOSE_FILE=docker-compose.yml

# Build Docker images
build:
	docker-compose -f $(COMPOSE_FILE) build

# Start containers in detached mode
up:
	docker-compose -f $(COMPOSE_FILE) up -d

# Stop and remove containers, networks, and volumes
down:
	docker-compose -f $(COMPOSE_FILE) down

# Restart all containers
restart: down up

# Show running containers
ps:
	docker ps

# Show logs of all services
logs:
	docker-compose -f $(COMPOSE_FILE) logs -f

# Remove all stopped containers, unused networks, images, and cache
prune:
	docker system prune -af
