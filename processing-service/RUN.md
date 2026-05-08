## 1. Сборка проекта

Собирает образ `processing-service` по `Dockerfile`.

```bash
docker compose build
```

## 2. Запуск Ollama

Запускает контейнер `ollama` в фоновом режиме.

```bash
docker compose up -d ollama
```

## 3. Загрузка модели Qwen2.5:7b

Скачивает модель `qwen2.5:7b` в `Ollama`.
Этот шаг нужен перед первым запуском суммаризации.

```bash
docker exec -it ollama ollama pull qwen2.5:7b
```

## 4. Проверка доступных моделей

Показывает список моделей, загруженных в `Ollama`.

```bash
docker exec -it ollama ollama list
```

## 5. Запуск processing-service

Запускает основной сценарий обработки аудио через `start.py`.

```bash
docker compose up processing-service
```

## 6. Запуск всех котейнеров

```bash
docker compose up
```

## 7. Копирование результатов из контейнера

Копирует структурированный конспект в локальную директорию.

```bash
docker cp processing-service:/app/summary.txt ./summary.txt
```

Копирует транскрипцию в локальную директорию.

```bash
docker cp processing-service:/app/output.txt ./output.txt
```

## 8. Просмотр логов

Показывает логи `processing-service`.

```bash
docker compose logs processing-service
```

Показывает логи `ollama`.

```bash
docker compose logs ollama
```

## 9. Остановка контейнеров

Останавливает и удаляет контейнеры, созданные через `docker compose`.

```bash
docker compose down
```

## 10. Полная очистка

Останавливает контейнеры и удаляет volume `ollama_data`.
После этого модель `qwen2.5:7b` нужно будет скачать заново.

```bash
docker compose down -v
```
