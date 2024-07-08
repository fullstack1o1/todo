
CREATE TABLE IF NOT EXISTS todo_users (
        user_id SERIAL PRIMARY KEY,
        username VARCHAR(50) NOT NULL UNIQUE,
        email VARCHAR(100) NOT NULL UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO todo_users (username, email, password_hash) VALUES ('user1', 'user1@test.dev', 'password1');
INSERT INTO todo_users (username, email, password_hash) VALUES ('user2', 'user2@test.dev', 'password2');

CREATE TYPE task_status AS ENUM ('pending', 'in_progress', 'completed');

CREATE TABLE IF NOT EXISTS tasks (
        task_id SERIAL PRIMARY KEY,
        user_id INT NOT NULL,
        title VARCHAR(255) NOT NULL,
        description TEXT,
        status task_status DEFAULT 'pending',  -- Use the enum type here
        due_date DATE,
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
       tag_id SERIAL PRIMARY KEY,
       name VARCHAR(50) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS task_tags (
        task_id INT NOT NULL,
        tag_id INT NOT NULL,
        PRIMARY KEY (task_id, tag_id),
        CONSTRAINT fk_task
            FOREIGN KEY(task_id)
                REFERENCES tasks(task_id)
                ON DELETE CASCADE,
        CONSTRAINT fk_tag
            FOREIGN KEY(tag_id)
                REFERENCES tags(tag_id)
                ON DELETE CASCADE
);
