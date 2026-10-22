# RedisApp

RedisApp — учебное приложение для обмена сообщениями на Spring Boot 4.1.1 и Java 25. Проект построен по принципам чистой архитектуры: доменные модели и сценарии отделены от контроллеров, JPA-хранилищ и инфраструктуры. Данные хранятся в PostgreSQL, а Redis используется для кеширования чатов, сообщений и результатов поиска, а также для хранения HTTP-сессий.

Приложение умеет регистрировать и аутентифицировать пользователей, создавать личные и групповые чаты, управлять участниками, отправлять сообщения, отмечать их прочитанными, искать чаты и сообщения. Удаление чатов и сообщений реализовано как логическое, а администратор может удалять чужие сообщения. Есть REST API и веб-интерфейс на Thymeleaf, защищённый Spring Security с CSRF-защитой.

## Структура проекта

Код разделён на слои: `domain` содержит бизнес-модели, `application` — сценарии и порты, `infrastructure` — реализации хранения, кеша и безопасности, `presentation` — REST API и веб-интерфейс.

```text
RedisApp/
├── src/main/java/ru/lakeevda/redisapp/
│   ├── domain/              # модели, enum'ы и доменные исключения
│   ├── application/         # сценарии, команды, порты и модели выборки
│   │   ├── port/in/         # входящие интерфейсы use case
│   │   └── port/out/        # исходящие интерфейсы репозиториев и кодировщика
│   ├── infrastructure/      # JPA, Redis-кеш, Spring Security, адаптеры
│   └── presentation/        # REST API, DTO и веб-контроллеры
├── src/main/resources/
│   ├── db/changelog/        # Liquibase-миграции
│   ├── templates/web/       # Thymeleaf-страницы
│   ├── static/web/          # CSS и JavaScript
│   └── application.properties
├── src/test/java/           # unit- и интеграционные тесты
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── .env.example
```

## Установка

Понадобятся JDK 25, Docker и Docker Compose. Скопируйте шаблон переменных окружения и при необходимости измените значения:

```bash
cp .env.example .env
docker compose up -d postgres redis
```

При локальном запуске PostgreSQL опубликован на порту `5433`, поэтому в `.env` следует указать `POSTGRES_PORT=5433`. При запуске приложения внутри Docker Compose используется внутренний порт `5432`.

## Запуск

Локальный запуск из корня проекта:

```bash
./mvnw spring-boot:run
```

Полный запуск вместе с приложением в контейнерах:

```bash
docker compose up --build
```

Приложение будет доступно по адресу `http://localhost:8080`. Регистрация через форму или API создаёт пользователя с ролью `USER`; для доступа к административным страницам роль можно изменить вручную:

```sql
UPDATE redis_app.users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

Проверка тестов: `./mvnw test`, интеграционные тесты — `./mvnw verify`.

## Пример запроса

```bash
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"email":"user@example.com","username":"user","password":"secret1","confirmPassword":"secret1"}'
```
