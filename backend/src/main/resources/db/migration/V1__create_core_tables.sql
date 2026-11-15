CREATE TABLE app_users
(
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(120) NOT NULL UNIQUE,
    display_name  VARCHAR(80)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE planner_tasks
(
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    title        VARCHAR(160) NOT NULL,
    description  VARCHAR(1000),
    due_date     DATE,
    completed    BOOLEAN      NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE
);
CREATE TABLE habits
(
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    name        VARCHAR(160) NOT NULL,
    description VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_habit_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE
);
CREATE TABLE habit_completions
(
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    habit_id        BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    completion_date DATE   NOT NULL,
    CONSTRAINT uq_habit_completion UNIQUE (habit_id, completion_date),
    CONSTRAINT fk_completion_habit FOREIGN KEY (habit_id) REFERENCES habits (id) ON DELETE CASCADE,
    CONSTRAINT fk_completion_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE
);
