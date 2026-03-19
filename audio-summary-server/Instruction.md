## Запуск проекта

### Локальный запуск

Сборка проекта:

```shell
mvn clean package
```

Запуск приложения:
mvn spring-boot:run

Проверка:
http://localhost:8080/health

---

### Запуск через Docker

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

Проверка:
http://localhost:8080/health