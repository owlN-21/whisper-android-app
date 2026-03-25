## Запуск проекта

### Локальный запуск

Сборка проекта:

```shell
mvn clean package
```

Запуск приложения:

```shell
mvn spring-boot:run
```

Проверка health endpoint:

```text
http://localhost:8080/health
```

Проверка через Postman или браузер:

* `GET http://localhost:8080/health`

---

### Подключение к PostgreSQL локально

Подключение к базе данных:

```shell
psql -U postgres -d audio_summary_db
```

После входа в `psql` можно выполнить команды для просмотра содержимого базы.

Посмотреть список таблиц:

```sql
\dt
```

Посмотреть структуру таблицы `users`:

```sql
\d users
```

Посмотреть всех пользователей:

```sql
select * from users;
```

Посмотреть только основные поля пользователей:

```sql
select id, email, created_at, updated_at from users;
```

Выйти из `psql`:

```sql
\q
```

---

### Проверка user endpoint'ов локально

Создание пользователя:

```http
POST http://localhost:8080/api/v1/users
Content-Type: application/json

{
  "email": "test@example.com"
}
```

Получение пользователя по id:

```http
GET http://localhost:8080/api/v1/users/1
```

Получение пользователя по email:

```http
GET http://localhost:8080/api/v1/users/by-email?email=test@example.com
```

После успешного `POST /api/v1/users` запись должна появиться в таблице `users`.

---

## Запуск через Docker

Сначала собрать jar:

```shell
mvn clean package
```

Потом собрать Docker-образ:

```shell
docker build -t audio-summary-server .
```

Запуск контейнера:

```shell
docker run -p 8080:8080 audio-summary-server
```

Проверка health endpoint:

```text
http://localhost:8080/health
```

Проверка через Postman или браузер:

* `GET http://localhost:8080/health`

---

### Полезные Docker-команды

Посмотреть запущенные контейнеры:

```shell
docker ps
```

Посмотреть все контейнеры:

```shell
docker ps -a
```

Посмотреть логи приложения:

```shell
docker logs <container_name>
```

Посмотреть логи в реальном времени:

```shell
docker logs -f <container_name>
```

Остановить контейнер:

```shell
docker stop <container_name>
```

Удалить контейнер:

```shell
docker rm <container_name>
```

---

### Проверка API после запуска в Docker

Создание пользователя:

```http
POST http://localhost:8080/api/v1/users
Content-Type: application/json

{
  "email": "test@example.com"
}
```

Получение пользователя по id:

```http
GET http://localhost:8080/api/v1/users/1
```

Получение пользователя по email:

```http
GET http://localhost:8080/api/v1/users/by-email?email=test@example.com
```

---

## Проверка данных в PostgreSQL после запросов

После успешного создания пользователя через API можно проверить наличие записи в базе.

Подключение к PostgreSQL:

```shell
psql -U postgres -d audio_summary_db
```

Проверка данных в таблице `users`:

```sql
select * from users;
```

или:

```sql
select id, email, created_at, updated_at from users;
```

---

## Минимальный сценарий ручной проверки

1. Запустить PostgreSQL.
2. Запустить backend локально или через Docker.
3. Проверить:

    * `GET http://localhost:8080/health`
4. Создать пользователя:

    * `POST http://localhost:8080/api/v1/users`
5. Проверить пользователя по id:

    * `GET http://localhost:8080/api/v1/users/1`
6. Проверить пользователя по email:

    * `GET http://localhost:8080/api/v1/users/by-email?email=test@example.com`
7. Подключиться к PostgreSQL:

    * `psql -U postgres -d audio_summary_db`
8. Проверить содержимое таблицы:

    * `select * from users;`

Если хочешь, я могу сразу привести это в вид аккуратного `README.md` блока без лишних пояснений, чтобы ты просто вставила его в проект.
