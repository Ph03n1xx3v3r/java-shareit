# ShareIt

ShareIt — сервис для обмена вещами. Репозиторий содержит два самостоятельных Spring Boot приложения в одном Maven multi-module проекте.

## Архитектура

- `shareIt-gateway` — внешний REST API на порту `8080`. Проверяет базовые входные данные и перенаправляет запросы в сервер.
- `shareIt-server` — бизнес-логика и доступ к PostgreSQL на порту `9090`.

## Требования

- Java 21
- Maven 3.9+
- PostgreSQL (для локального запуска сервера)

## Сборка и тесты

Из корневой директории:

```bash
mvn clean verify
```

## Запуск

Сначала запустите PostgreSQL и создайте базу `shareit`, затем в разных терминалах выполните:

```bash
mvn -pl shareIt-server spring-boot:run
```

```bash
mvn -pl shareIt-gateway spring-boot:run
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
