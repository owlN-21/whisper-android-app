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

Запуск контейнера с файлом .env:
```shell
docker run --env-file src/main/resources/.env -p 8080:8080 audio-summary-server
```
Запуск:
```shell
docker compose up --build
```
запуск контейнера audiosummary-postgres:
```shell
docker exec -it audiosummary-postgres psql -U ${APP_USER} -d ${APP_DB}
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

## Минимальный сценарий ручной проверки user логики

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

---

## Проверка task endpoint'ов локально

Сначала нужно создать пользователя, так как задача всегда привязывается к существующему пользователю!

Создание задачи с загрузкой аудиофайла:

```http
POST http://localhost:8080/api/users/1/tasks
Content-Type: multipart/form-data
```

В Postman:

* выбрать `POST`
* указать URL `http://localhost:8080/api/users/1/tasks`
* открыть вкладку `Body`
* выбрать `form-data`
* добавить поле:

    * key: `file`
    * type: `File`
    * value: выбрать аудиофайл

Важно:

* имя поля должно быть именно `file`
* поддерживаемые форматы `mp3`, `wav`, `m4a`

Получение задачи по id:

```http
GET http://localhost:8080/api/tasks/1
```

Получение списка задач пользователя:

```http
GET http://localhost:8080/api/users/1/tasks
```

Удаление задачи:

```http
DELETE http://localhost:8080/api/tasks/1
```

После успешного `POST /api/users/{userId}/tasks`:

* файл должен сохраниться в локальное хранилище
* запись должна появиться в таблице `processing_tasks`
* задача должна получить статус `UPLOADED`

# В таблице processing_tasks:

`original_filename` — исходное имя файла
`storage_path` — путь к сохраненному файлу

После успешного `DELETE /api/tasks/{taskId}`:

* файл должен быть удален из хранилища
* запись должна быть удалена из таблицы `processing_tasks`

---

### Проверка task данных в PostgreSQL

Подключение к PostgreSQL:

```shell
psql -U postgres -d audio_summary_db
```

Посмотреть все задачи:

```sql
select * from processing_tasks;
```

Посмотреть основные поля задач:

```sql
select id, user_id, original_filename, storage_path, status, error_message, created_at, updated_at from processing_tasks;
```

Посмотреть структуру таблицы `processing_tasks`:

```sql
\d processing_tasks
```

---

### Проверка task API после запуска в Docker

Создание задачи с загрузкой аудиофайла:

```http
POST http://localhost:8080/api/users/1/tasks
Content-Type: multipart/form-data
```

Получение задачи по id:

```http
GET http://localhost:8080/api/tasks/1
```

Получение списка задач пользователя:

```http
GET http://localhost:8080/api/users/1/tasks
```

Удаление задачи:

```http
DELETE http://localhost:8080/api/tasks/1
```
### Если при загрузке файла приходит 413 Request Entity Too Large (Размер указан в application.yml)

---

## Минимальный сценарий ручной проверки task логики

1. Запустить PostgreSQL.
2. Запустить backend локально или через Docker.
3. Проверить:

    * `GET http://localhost:8080/health`
4. Создать пользователя:

    * `POST http://localhost:8080/api/v1/users`
5. Загрузить аудиофайл:

    * `POST http://localhost:8080/api/users/{userId}/tasks`
6. Проверить задачу по id:

    * `GET http://localhost:8080/api/tasks/{taskId}`
7. Проверить список задач пользователя:

    * `GET http://localhost:8080/api/users/{userId}/tasks`
8. Проверить данные в PostgreSQL:

    * `select * from processing_tasks;`
9. Удалить задачу:

    * `DELETE http://localhost:8080/api/tasks/{taskId}`
10. Повторно проверить таблицу:

    * `select * from processing_tasks;`


### Минимальный сценарий ручной проверки summary логики в Docker

1. Запустить контейнеры:

```shell
docker compose up --build
```

2. Проверить health endpoint:

```http
GET http://localhost:8080/health
```

3. Создать пользователя:

```http
POST http://localhost:8080/api/v1/users
Content-Type: application/json

{
  "email": "test-summary@example.com"
}
```

4. Загрузить аудиофайл:

```http
POST http://localhost:8080/api/users/{userId}/tasks
Content-Type: multipart/form-data
```

В Postman:

* выбрать `Body`
* выбрать `form-data`
* добавить поле `file`
* тип поля `File`
* выбрать аудиофайл

5. Проверить, что задача сохранилась:

```http
GET http://localhost:8080/api/users/{userId}/tasks
```

6. Подключиться к PostgreSQL внутри Docker:

```shell
docker exec -it audiosummary-postgres psql -U ${APP_USER} -d ${APP_DB}
```

7. Проверить таблицу `processing_tasks`:

```sql
select * from processing_tasks;
```

8. Проверить таблицу `summaries`:

```sql
select * from summaries;
```

9. Проверить основные поля summary:

```sql
select id, task_id, content, created_at from summaries;
```

После успешного `POST /api/users/{userId}/tasks`:

* запись должна появиться в таблице `processing_tasks`
* запись должна появиться в таблице `summaries`
* `summaries.task_id` должен ссылаться на созданную задачу
* в поле `content` должен сохраниться текст заглушки

10. Удалить задачу:

```http
DELETE http://localhost:8080/api/tasks/{taskId}
```

11. Повторно проверить таблицы:

```sql
select * from processing_tasks;
select * from summaries;
```

После успешного `DELETE /api/tasks/{taskId}`:

* запись должна быть удалена из таблицы `processing_tasks`
* связанная запись в таблице `summaries` должна удалиться автоматически

### Проверка обработки аудио через main backend

1. Создать пользователя

POST `http://localhost:8080/api/v1/users`

Body → raw → JSON:

```json
{
  "email": "test@example.com"
}
```

2. Загрузить аудио

POST `http://localhost:8080/api/users/{userId}/tasks`

Body → form-data:

key: `file`
type: `File`
value: выбрать `.mp3`, `.wav` или `.m4a`

Важно: `taskId` и `model` сюда не передавать.

3. Проверить статус задачи

GET `http://localhost:8080/api/tasks/{taskId}`

Повторять запрос, пока статус не станет `COMPLETED`.

Статусы:

`TRANSCRIBING` → идет транскрибация
`SUMMARIZING` → идет суммаризация
`COMPLETED` → результат готов
`FAILED` → ошибка, смотреть `errorMessage`

4. Получить summary

GET `http://localhost:8080/api/tasks/{taskId}/result`

5. Удалить задачу

DELETE `http://localhost:8080/api/tasks/{taskId}`

Коротко по логике: после `POST /api/users/{userId}/tasks` задача только запускается. Чтобы transcript и summary подтянулись и сохранились в БД, нужно дергать `GET /api/tasks/{taskId}`.
