-- Добавляем проверку рейтинга
ALTER TABLE movies ADD CONSTRAINT imdb_rating_check
    CHECK (imdb_rating ~ '^[0-9]+(\.[0-9]+)?$' OR imdb_rating = 'N/A');

-- Добавляем индексы для ускорения поиска
CREATE INDEX idx_movies_title ON movies(title);
CREATE INDEX idx_movies_genre ON movies(genre);
CREATE INDEX idx_collections_user ON collections(user_id);