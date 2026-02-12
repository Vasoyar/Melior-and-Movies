# Melior and movie
Приложение для рекомендации фильмов

## Авторизация
### Регистрация пользователя

Обоснование: первый шаг для нового пользователя. Создает профиль с базовыми настройками вкуса. Отдельный endpoint для регистрации, так как требует валидации email/username на уникальность.

Method: POST. 

URL: /api/auth/register

Коды ответов:

· 200 OK - пользователь успешно зарегистрирован. 

· 400 Bad Request - ошибка валидации данных. 

· 409 Conflict - пользователь с таким username/email уже существует.  

Заголовки запроса:  

```text
Host: localhost:8080
Connection: keep-alive
User-Agent: MovieRecommender-WebApp/1.0
Accept: application/json
Accept-Language: ru-RU, ru;q=0.9, en;q=0.8
Accept-Encoding: gzip, deflate
Accept-Charset: utf-8
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
```


Тело запроса:

```json
{
  "username": "string (уникальный, 3-50 символов)",
  "email": "string (уникальный, email формат)",
  "password": "string (мин. 8 символов)"
}
```
Заголовки ответа (200):

```text
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
Content-Language: ru-RU
Content-Encoding: gzip
Server: Spring Boot MovieRecommender/1.0
X-User-ID: 123
X-Request-ID: abc-123-def
```

Тело ответа (200):

```json
{
  "message": "User registered successfully"
}
```

Тело ответа (400):

```json
{
  "error": "Validation error",
  "details": ["Username must be between 3 and 50 characters"]
}
```
### Вход в систему  

Обоснование: После регистрации пользователь должен аутентифицироваться. Этот endpoint возвращает JWT токен (в будущей реализации). Сейчас заглушка, так как JWT еще не настроен.

Method: POST. 

URL: /api/auth/login

Коды ответов:

· 200 OK - успешный вход. 

· 401 Unauthorized - неверные учетные данные.  

Заголовки запроса:

```text
Host: localhost:8080
Connection: keep-alive
User-Agent: Mozilla/5.0 (MovieRecommender Mobile) 
Accept: application/json
Accept-Language: ru-RU
Accept-Encoding: gzip
Accept-Charset: utf-8
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
```

Тело запроса:

```json
{
  "username": "string",
  "password": "string"
}
```

Заголовки ответа (200):

```text
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
Content-Language: ru-RU
Server: Spring Boot MovieRecommender/1.0
X-Auth-Status: authenticated
Cache-Control: no-store
```

Тело ответа (200):

```json
{
  "message": "User logined successfully"
}
```
Заголовки ответа (401):  

```text
Content-Type: application/json; charset=utf-8
WWW-Authenticate: Bearer realm="MovieRecommender"
Content-Length: [автоматически]
Server: Spring Boot MovieRecommender/1.0
```

Тело ответа (401):

```json
{
  "error": "Invalid credentials"
}
```

---

## Фильмы

### Тестовый endpoint контроллера

Обоснование: Простой endpoint для проверки, что контроллер фильмов работает. Полезен для мониторинга и отладки.

Method: GET. 

URL: /api/movies/test

Коды ответов: 200 OK

Заголовки запроса:  

```text
Host: localhost:8080
Connection: keep-alive
User-Agent: MovieRecommender-TestClient/1.0
Accept: application/json, text/plain
Accept-Language: ru-RU, en-US
Accept-Encoding: identity
Accept-Charset: utf-8
```

Заголовки ответа:

```text
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
Content-Language: ru-RU
Server: Spring Boot MovieRecommender/1.0
X-Response-Time: 45ms
Cache-Control: no-cache
```

Тело ответа:

```json
"Movie controller is working!"
```

---

## Рекомендации

### Получить следующий фильм для рекомендации  

Обоснование: основной endpoint приложения. Возвращает персонализированную рекомендацию с AI-объяснением. Контекстный параметр позволяет адаптировать рекомендации под время суток/настроение.  

Method: GET. 

URL: /api/recommendations/next

Коды ответов:

· 200 OK - есть рекомендация. 

· 204 No Content - нет доступных рекомендаций.  

Параметры запроса:

| Параметр | Описание | Пример |
| :--- | :---: | ---: |
| context | Контекст просмотра | context=evening |



Заголовки запроса:

```text
Host: localhost:8080
Connection: keep-alive
User-Agent: MovieRecommender-WebApp/1.0
Accept: application/json
Accept-Language: ru-RU
Accept-Encoding: gzip, deflate
Accept-Charset: utf-8
X-User-ID: 123 (в будущем - Authorization: Bearer <token>)
```
Заголовки ответа (200):

```text
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
Content-Language: ru-RU
Content-Encoding: gzip
Server: Spring Boot MovieRecommender/1.0
X-Total-Available: 25
X-Match-Score: 0.87
X-Recommendation-ID: rec-123
X-Cache: HIT (или MISS)
X-Response-Time: 120ms
Cache-Control: private, max-age=60
```

Тело ответа (200):

```json
{
  "id": 123,
  "title": "Название фильма",
  "overview": "Описание фильма...",
  "genres": ["драма", "триллер"],
  "voteAverage": 8.5,
  "posterPath": "/path/to/poster.jpg",
  "backdropPath": "/path/to/backdrop.jpg",
  "releaseDate": "2020-01-01",
  "trailerUrl": "https://youtube.com/watch?v=...",
  "features": {
    "сюжет": 0.9,
    "визуальные_эффекты": 0.8
  },
  "matchScore": 0.87,
  "aiExplanation": "На 87% соответствует вашему вкусу из-за стиля диалогов..."
}
```

Заголовки ответа (204):

```text
Content-Type: application/json; charset=utf-8
Content-Length: 0
Content-Language: ru-RU
Server: Spring Boot MovieRecommender/1.0
X-Total-Available: 0
X-Response-Time: 30ms
Retry-After: 300
```

---

### Обработать свайп пользователя

Обоснование: Ключевой endpoint для обучения алгоритма. Каждый свайп обновляет профиль пользователя.

Method: POST. 

URL: /api/recommendations/swipe

Коды ответов: 200 OK. 

Заголовки запроса:

```text
Host: localhost:8080
Connection: keep-alive
User-Agent: MovieRecommender-MobileApp/2.0
Accept: application/json
Accept-Language: ru-RU
Accept-Encoding: gzip
Accept-Charset: utf-8
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
X-Swipe-Timestamp: 2024-01-28T12:00:00Z
```

Тело запроса:

```json
{
  "movieId": 123,
  "liked": true
}
```

Заголовки ответа:

```text
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
Content-Language: ru-RU
Server: Spring Boot MovieRecommender/1.0
X-Response-Time: 25ms
X-Profile-Updated: true
X-Recommendations-Affected: 15
```

Тело ответа:

```json
{
  "status": "processed",
  "profileUpdated": true
}
```


---

### Получить рекомендации для коллекции

Обоснование: пользователи создают тематические коллекции. Этот endpoint помогает подобрать фильмы для конкретной темы (например, "осенние фильмы"). Использует тематический фильтр поверх основного алгоритма.

Method: GET. 

URL: /api/recommendations/collections

Коды ответов: 200 OK. 


Параметры запроса:

| Параметр | Описание | Пример |
| :--- | :---: | ---: |
| theme | Тема коллекции | theme=осенние_фильмы |


Заголовки запроса:

```text
Host: localhost:8080
Connection: keep-alive
User-Agent: MovieRecommender-WebApp/1.0
Accept: application/json
Accept-Language: ru-RU
Accept-Encoding: gzip, deflate
Accept-Charset: utf-8
X-Collection-Theme: осенние_фильмы
```

Заголовки ответа:

```text
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
Content-Language: ru-RU
Content-Encoding: gzip
Server: Spring Boot MovieRecommender/1.0
X-Total-Recommendations: 10
X-Theme: осенние_фильмы
X-Response-Time: 150ms
Cache-Control: public, max-age=300
```

Тело ответа:

```json
[
  {
    "id": 123,
    "title": "Фильм 1",
    "overview": "Описание...",
    "genres": ["драма"],
    "voteAverage": 8.2,
    "posterPath": "/poster1.jpg",
    "matchScore": 0.91
  },
  {
    "id": 456,
    "title": "Фильм 2",
    "overview": "Описание...",
    "genres": ["триллер"],
    "voteAverage": 7.8,
    "posterPath": "/poster2.jpg",
    "matchScore": 0.87
  }
]
```

---

## Коллекции

### Создать новую коллекцию

Обоснование: Пользователи могут создавать тематические подборки. Возвращает созданную коллекцию с ID для последующего использования.

Method: POST. 

URL: /api/collections

Коды ответов:

· 200 OK - коллекция создана успешно. 

· 400 Bad Request - ошибка валидации. 

Заголовки запроса:

```text
Host: localhost:8080
Connection: keep-alive
User-Agent: MovieRecommender-WebApp/1.0
Accept: application/json
Accept-Language: ru-RU
Accept-Encoding: gzip
Accept-Charset: utf-8
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
X-User-ID: 123
```

Тело запроса:

```json
{
  "title": "Фильмы для осенней хандры",
  "description": "Атмосферные фильмы для дождливых осенних дней"
}
```

Заголовки ответа (200):

```text
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
Content-Language: ru-RU
Server: Spring Boot MovieRecommender/1.0
Location: /api/collections/1
X-Collection-ID: 1
X-Response-Time: 80ms
```

Тело ответа (200):

```json
{
  "id": 1,
  "title": "Фильмы для осенней хандры",
  "description": "Атмосферные фильмы для дождливых осенних дней",
  "createdAt": "2024-01-28T12:00:00Z"
}
```



---

### Добавить фильм в коллекцию

Обоснование: после создания коллекции пользователь добавляет в нее фильмы. Для каждой коллекции и фильма используются свои ID.  


Method: POST. 

URL: /api/collections/{collectionId}/movies/{movieId}

Коды ответов:

· 200 OK - фильм успешно добавлен. 

· 404 Not Found - коллекция не найдена. 

| ЗПараметр | Описание | Пример |
| :--- | :---: | ---: |
| collectionId | ID коллекции | 1 |
| movieId | ID фильма | 123 |


Заголовки запроса:

```text
Host: localhost:8080
Connection: keep-alive
User-Agent: MovieRecommender-MobileApp/2.0
Accept: application/json
Accept-Language: ru-RU
Accept-Encoding: gzip
Accept-Charset: utf-8
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
```

Заголовки ответа (200):

```text
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
Content-Language: ru-RU
Server: Spring Boot MovieRecommender/1.0
X-Collection-Size: 5
X-Response-Time: 40ms
```

Тело ответа (200):

```json
{
  "message": "Movie added to collection successfully",
  "collectionId": 1,
  "movieId": 123
}
```

Заголовки ответа (404):

```text
Content-Type: application/json; charset=utf-8
Content-Length: [автоматически]
Content-Language: ru-RU
Server: Spring Boot MovieRecommender/1.0
X-Error-Type: collection_not_found
```

Тело ответа (404):

```json
{
  "error": "Collection not found",
  "collectionId": 999
}
```



---

