CREATE TABLE IF NOT EXISTS todo_users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO todo_users (username, email, password_hash) VALUES ('user1', 'user1@test.dev', 'password1');
INSERT INTO todo_users (username, email, password_hash) VALUES ('user2', 'user2@test.dev', 'password2');

CREATE TABLE IF NOT EXISTS tasks (
    task_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(255), -- DEFAULT 'PENDING',
    due_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user
    FOREIGN KEY(user_id)
    REFERENCES todo_users(user_id)
    ON DELETE CASCADE
);

CREATE INDEX idx_user_id ON tasks(user_id);
CREATE INDEX idx_status ON tasks(status);

CREATE TABLE IF NOT EXISTS tags (
    id SERIAL PRIMARY KEY,
    task_id INT NOT NULL,
    user_id INT NOT NULL,
    name VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT fk_task
        FOREIGN KEY(task_id)
            REFERENCES tasks(task_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
            REFERENCES todo_users(user_id)
            ON DELETE CASCADE
);
