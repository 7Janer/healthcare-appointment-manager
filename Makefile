.PHONY: up down logs test frontend-test

up:
	docker compose up --build

down:
	docker compose down

logs:
	docker compose logs -f

test:
	mvn test

frontend-test:
	cd frontend && npm ci && npm run build
