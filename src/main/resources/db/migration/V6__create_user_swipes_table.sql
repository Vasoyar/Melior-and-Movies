-- Таблица для хранения истории свайпов пользователей
CREATE TABLE IF NOT EXISTS user_swipes (
                                           id BIGSERIAL PRIMARY KEY,
                                           user_id BIGINT NOT NULL,
                                           movie_id VARCHAR(20) NOT NULL,
    liked BOOLEAN NOT NULL,
    swiped_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(imdb_id) ON DELETE CASCADE
    );

-- Индексы для быстрого поиска
CREATE INDEX idx_user_swipes_user ON user_swipes(user_id);
CREATE INDEX idx_user_swipes_movie ON user_swipes(movie_id);
CREATE INDEX idx_user_swipes_user_movie ON user_swipes(user_id, movie_id);