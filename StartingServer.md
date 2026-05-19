## Docker network
Docker network — это общая “виртуальная сеть” для контейнеров.

Она нужна, чтобы контейнеры могли обращаться друг к другу **по имени сервиса**, а не через `localhost`.

Главное правило:

`localhost` внутри контейнера — это сам контейнер.

То есть если backend в Docker попробует сходить на:

`http://localhost:8090`

он будет искать processing-service **внутри самого backend-контейнера**, а не рядом.

Поэтому для связи контейнеров мы используем общую сеть:

`audio-summary-net`

И backend ходит к processing-service так:

`http://processing-service:8090`

---

## Как создать общую сеть

Один раз выполнить:

```bash
docker network create audio-summary-net
```

Если сеть уже существует — ничего страшного.

---

## Как использовать сеть в двух docker-compose

В compose processing-service:

```yaml
services:
  processing-service:
    networks:
      - audio-summary-net

  ollama:
    networks:
      - audio-summary-net

networks:
  audio-summary-net:
    external: true
```

В compose основного backend:

```yaml
services:
  audiosummary:
    networks:
      - audio-summary-net
    environment:
      APP_PROCESSING_SERVICE_BASE_URL: http://processing-service:8090

  postgres:
    networks:
      - audio-summary-net

networks:
  audio-summary-net:
    external: true
```

Главное — чтобы оба compose были подключены к одной сети `audio-summary-net`.

---

## Как запускать

Сначала создать сеть, если еще не создана:

```bash
docker network create audio-summary-net
```

Потом запустить processing-service:

```bash
cd processing-service
docker compose up --build
```

В другом терминале запустить основной backend:

```bash
cd server
mvn clean package
docker compose up --build
```

Если хочешь запускать в фоне:

```bash
docker compose up --build -d
```

Посмотреть контейнеры:

```bash
docker ps
```

Посмотреть логи backend:

```bash
docker logs audiosummary-app -f
```

Посмотреть логи processing-service:

```bash
docker logs processing-service -f
```

---

## Как проверить, что сеть работает

С компьютера processing-service доступен так:

`http://localhost:8090/docs`

А backend внутри Docker обращается к нему так:

`http://processing-service:8090`

В `application.yml` можно оставить дефолт:

```yaml
app:
  processing-service:
    base-url: ${APP_PROCESSING_SERVICE_BASE_URL:http://localhost:8090}
```

А в Docker через `environment` переопределять:

```yaml
APP_PROCESSING_SERVICE_BASE_URL: http://processing-service:8090
```

---

## Основной pipeline запросов для Android

### 1. Создать пользователя

Android отправляет:

`POST /api/v1/users`

Body JSON:

```json
{
  "email": "test@example.com"
}
```

Ответ:

```json
{
  "id": 7,
  "email": "test@example.com",
  "createdAt": "...",
  "updatedAt": "..."
}
```

---

### 2. Загрузить аудио

Android отправляет:

`POST /api/v1/users/{userId}/tasks`

Body: `multipart/form-data`

Поле:

`file` — аудиофайл `.mp3`, `.wav` или `.m4a`

Важно: Android не передает `taskId` и `model`.

Backend сам:

* создает `taskId`;
* сохраняет файл;
* отправляет аудио в processing-service;
* ставит статус `TRANSCRIBING`.

Ответ:

```json
{
  "id": 26,
  "userId": 7,
  "originalFilename": "audio.mp3",
  "status": "TRANSCRIBING",
  "errorMessage": null,
  "createdAt": "...",
  "updatedAt": "..."
}
```

---

### 3. Проверять статус задачи

Android периодически вызывает:

`GET /api/v1/tasks/{taskId}`

Ответ может быть:

```json
{
  "id": 26,
  "userId": 7,
  "originalFilename": "audio.mp3",
  "status": "TRANSCRIBING",
  "errorMessage": null,
  "createdAt": "...",
  "updatedAt": "..."
}
```

Возможные статусы:

`TRANSCRIBING` — идет распознавание аудио
`SUMMARIZING` — идет создание summary
`COMPLETED` — обработка завершена
`FAILED` — ошибка, смотреть `errorMessage`

Android должен повторять `GET /api/v1/tasks/{taskId}`, пока не получит `COMPLETED` или `FAILED`.

---

### 4. Получить transcript

Когда статус стал `COMPLETED`:

`GET /api/v1/tasks/{taskId}/transcript`

Ответ:

```json
{
  "taskId": 26,
  "status": "COMPLETED",
  "transcript": "Полный распознанный текст..."
}
```

---

### 5. Получить summary

Когда статус стал `COMPLETED`:

`GET /api/v1/tasks/{taskId}/result`

Ответ:

```json
{
  "taskId": 26,
  "status": "COMPLETED",
  "summary": "Краткое содержание..."
}
```

---

### 6. Удалить задачу

Если нужно удалить задачу:

`DELETE /api/v1/tasks/{taskId}`

Ответ:

`204 No Content`

При этом backend удаляет:

* задачу из `processing_tasks`;
* transcript и summary через `ON DELETE CASCADE`;
* аудиофайл из upload-директории.

---

Коротко весь Android-flow:

```text
POST /api/v1/users
POST /api/v1/users/{userId}/tasks
GET  /api/v1/tasks/{taskId}
GET  /api/v1/tasks/{taskId}/transcript
GET  /api/v1/tasks/{taskId}/result
DELETE /api/v1/tasks/{taskId}
```
