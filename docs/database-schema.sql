-- FamilyQ MariaDB Schema
-- 실행 전, 적절한 데이터베이스(familyq 등)를 선택하거나 생성하세요.

CREATE TABLE families (
    id BIGINT NOT NULL AUTO_INCREMENT,
    family_code VARCHAR(8) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_family_code (family_code)
) ENGINE = InnoDB;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    birth_year INT NOT NULL,
    role_type VARCHAR(20) NOT NULL,
    family_id BIGINT DEFAULT NULL,
    is_admin TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_user_login_id UNIQUE (login_id),
    CONSTRAINT fk_user_family FOREIGN KEY (family_id) REFERENCES families (id)
) ENGINE = InnoDB;

CREATE TABLE questions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    text VARCHAR(500) NOT NULL,
    order_index INT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_question_order UNIQUE (order_index)
) ENGINE = InnoDB;

CREATE TABLE family_questions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    family_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    sequence_number INT NOT NULL,
    assigned_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    completed_at DATETIME(6) DEFAULT NULL,
    required_member_count INT NOT NULL,
    insight_json LONGTEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_family_question_family FOREIGN KEY (family_id) REFERENCES families (id),
    CONSTRAINT fk_family_question_question FOREIGN KEY (question_id) REFERENCES questions (id),
    CONSTRAINT uk_family_question_sequence UNIQUE (family_id, sequence_number)
) ENGINE = InnoDB;

CREATE TABLE answers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    family_question_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content LONGTEXT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_answer_family_question FOREIGN KEY (family_question_id) REFERENCES family_questions (id),
    CONSTRAINT fk_answer_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_family_question_user UNIQUE (family_question_id, user_id)
) ENGINE = InnoDB;
