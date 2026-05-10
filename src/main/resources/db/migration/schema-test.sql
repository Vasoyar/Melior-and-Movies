-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Таблица предпочтений (если нужна)
CREATE TABLE IF NOT EXISTS user_preferences (
                                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                user_id BIGINT UNIQUE NOT NULL,
                                                action_pref DECIMAL(3,2) DEFAULT 0.50,
    comedy_pref DECIMAL(3,2) DEFAULT 0.50,
    drama_pref DECIMAL(3,2) DEFAULT 0.50,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );