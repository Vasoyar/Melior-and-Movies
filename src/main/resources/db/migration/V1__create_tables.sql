-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Таблица фильмов
CREATE TABLE IF NOT EXISTS movies (
                                      imdb_id VARCHAR(20) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    year VARCHAR(10),
    genre VARCHAR(1000),
    plot TEXT,
    poster VARCHAR(500),
    imdb_rating VARCHAR(10),
    director VARCHAR(255),
    runtime VARCHAR(50),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Таблица предпочтений
CREATE TABLE IF NOT EXISTS user_preferences (
                                                id BIGSERIAL PRIMARY KEY,
                                                user_id BIGINT UNIQUE NOT NULL,
                                                action_pref DECIMAL(3,2) DEFAULT 0.50,
    comedy_pref DECIMAL(3,2) DEFAULT 0.50,
    drama_pref DECIMAL(3,2) DEFAULT 0.50,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Таблица коллекций
CREATE TABLE IF NOT EXISTS collections (
                                           id BIGSERIAL PRIMARY KEY,
                                           title VARCHAR(100) NOT NULL,
    description TEXT,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

-- Таблица связи коллекций и фильмов
CREATE TABLE IF NOT EXISTS collection_movies (
                                                 collection_id BIGINT NOT NULL,
                                                 movie_id VARCHAR(20) NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (collection_id, movie_id),
    FOREIGN KEY (collection_id) REFERENCES collections(id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(imdb_id) ON DELETE CASCADE
    );

