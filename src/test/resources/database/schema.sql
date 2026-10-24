CREATE TABLE IF NOT EXISTS test_users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS test_user_profiles (
    -- 逻辑外键：由应用层保证 user_id 对应 test_users.id
    user_id BIGINT PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS test_user_addresses (
    id BIGINT PRIMARY KEY,
    -- 逻辑外键：由应用层保证 user_id 对应 test_users.id
    user_id BIGINT NOT NULL,
    label VARCHAR(30) NOT NULL,
    address VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_test_user_addresses_user_id (user_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS test_projects (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS test_user_projects (
    -- 逻辑外键：由应用层保证 user_id 对应 test_users.id
    user_id BIGINT NOT NULL,
    -- 逻辑外键：由应用层保证 project_id 对应 test_projects.id
    project_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, project_id),
    INDEX idx_test_user_projects_project_id (project_id)
) ENGINE = InnoDB;
