# ShareIt

ShareIt — сервис для обмена вещами. Репозиторий содержит два самостоятельных Spring Boot приложения в одном Maven multi-module проекте.

## Архитектура

- `gateway` — внешний REST API на порту `8080`. Проверяет базовые входные данные и перенаправляет запросы в сервер.
- `server` — бизнес-логика и доступ к PostgreSQL на порту `9090`.

Структура модулей (`server` и `gateway`) соответствует Docker Compose и CI-проверкам проекта.

## Требования

- Java 21
- Maven 3.9+
- Docker Compose (для запуска всего приложения)

## Сборка и тесты

Из корневой директории:

```bash
mvn clean verify
```

## Запуск через Docker Compose

Из корневой директории:

```bash
mvn clean package -DskipTests
docker compose build
docker compose up
```

Gateway будет доступен на `http://localhost:8080`, сервер — на `http://localhost:9090`.

## Локальный запуск без Docker

Сначала запустите PostgreSQL и создайте базу `shareit`, затем в разных терминалах выполните:

```bash
mvn -pl server spring-boot:run
```

```bash
mvn -pl gateway spring-boot:run
```

Gateway использует `http://localhost:9090` по умолчанию. Адрес можно изменить через `SHAREIT_SERVER_URL`.

Параметры подключения сервера к базе настраиваются переменными окружения `DB_HOST`, `DB_PORT`, `DB_NAME`, `POSTGRES_USER` и `POSTGRES_PASSWORD`.

## Запросы вещей

- `POST /requests` — создать запрос (`X-Sharer-User-Id`).
- `GET /requests` — запросы текущего пользователя с ответами (`X-Sharer-User-Id`).
- `GET /requests/all` — запросы других пользователей (`X-Sharer-User-Id`).
- `GET /requests/{requestId}` — один запрос с ответами.
- `POST /items` поддерживает необязательное поле `requestId`, связывающее вещь с запросом.

Списки запросов поддерживают параметры `from` (по умолчанию `0`) и `size` (по умолчанию `10`).
