-- Таблица для лайков коллекций
CREATE TABLE IF NOT EXISTS collection_likes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    collection_id BIGINT NOT NULL,
    liked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (collection_id) REFERENCES collections(id) ON DELETE CASCADE,
    UNIQUE(user_id, collection_id)
);

-- Индекс для быстрого подсчета лайков
CREATE INDEX idx_collection_likes_collection ON collection_likes(collection_id);
CREATE INDEX idx_collection_likes_user ON collection_likes(user_id);