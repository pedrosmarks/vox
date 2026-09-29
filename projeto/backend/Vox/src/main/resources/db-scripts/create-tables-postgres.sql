CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS event_image CASCADE;
DROP TABLE IF EXISTS event CASCADE;
DROP TABLE IF EXISTS event_category CASCADE;
DROP TABLE IF EXISTS audit_log CASCADE;
DROP TABLE IF EXISTS user_settings CASCADE;
DROP TABLE IF EXISTS project_signature CASCADE;
DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS project_status_history CASCADE;
DROP TABLE IF EXISTS project_moderation CASCADE;
DROP TABLE IF EXISTS project_opinion CASCADE;
DROP TABLE IF EXISTS project_councilor CASCADE;
DROP TABLE IF EXISTS project_image CASCADE;
DROP TABLE IF EXISTS project CASCADE;
DROP TABLE IF EXISTS issue_image CASCADE;
DROP TABLE IF EXISTS issue_report CASCADE;
DROP TABLE IF EXISTS issue_moderation CASCADE;
DROP TABLE IF EXISTS issue_status_history CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS subscription CASCADE;
DROP TABLE IF EXISTS password_reset_token CASCADE;
DROP TABLE IF EXISTS room_participant CASCADE;
DROP TABLE IF EXISTS conference_room CASCADE;
DROP TABLE IF EXISTS user_model CASCADE;
DROP TABLE IF EXISTS municipality CASCADE;

DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS project_type CASCADE;
DROP TYPE IF EXISTS project_nature CASCADE;
DROP TYPE IF EXISTS project_status CASCADE;
DROP TYPE IF EXISTS moderation_action CASCADE;
DROP TYPE IF EXISTS moderation_status CASCADE;
DROP TYPE IF EXISTS issue_status CASCADE;
DROP TYPE IF EXISTS notification_type CASCADE;
DROP TYPE IF EXISTS vote_type CASCADE;
DROP TYPE IF EXISTS subscription_type CASCADE;
DROP TYPE IF EXISTS room_status CASCADE;
DROP TYPE IF EXISTS participant_status CASCADE;
DROP TYPE IF EXISTS accessibility_mode CASCADE;
DROP TYPE IF EXISTS speech_request_status CASCADE;

CREATE TYPE user_role AS ENUM (
    'CITIZEN',
    'COUNCILOR',
    'MODERATOR',
    'ADMINISTRATOR'
);

CREATE TYPE project_nature AS ENUM (
    'LAW',
    'PUBLIC_WORK'
);

CREATE TYPE project_type AS ENUM (
    'CITIZEN',
    'CHAMBER'
);

CREATE TYPE project_status AS ENUM (
    'PENDING_APPROVAL',
    'REJECTED',
    'PUBLISHED',
    'IN_VOTING',
    'SELECTED_BY_COUNCIL',
    'APPROVED_BY_COUNCIL',
    'IN_EXECUTION',
    'COMPLETED',
    'ARCHIVED',
    'CANCELLED'
);

CREATE TYPE moderation_action AS ENUM (
    'APPROVED',
    'REJECTED'
);

CREATE TYPE moderation_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED'
);

CREATE TYPE issue_status AS ENUM (
    'OPEN',
    'UNDER_REVIEW',
    'IN_PROGRESS',
    'FORWARDED',
    'RESOLVED',
    'REJECTED',
    'CLOSED'
);

CREATE TYPE notification_type AS ENUM (
    'PROJECT_CREATED',
    'PROJECT_UPDATED',
    'PROJECT_STATUS_CHANGED',

    'ISSUE_CREATED',
    'ISSUE_UPDATED',
    'ISSUE_STATUS_CHANGED',

    'PROJECT_TAGGED',
    'ISSUE_TAGGED'
);

CREATE TYPE vote_type AS ENUM (
    'APPROVE',
    'NEUTRAL'
);

CREATE TYPE subscription_type AS ENUM (
    'ALL_PROJECTS',
    'ALL_ISSUES',
    'PROJECT',
    'ISSUE',
    'CATEGORY',
    'COUNCILOR'
);

CREATE TYPE accessibility_mode AS ENUM (
    'NONE',
    'DARK',
    'HIGH_CONTRAST',
    'PROTANOPIA',
    'DEUTERANOPIA',
    'TRITANOPIA'
);

CREATE TABLE municipality (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    state CHAR(2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(name, state)
);

CREATE TABLE user_model (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    cpf varchar(14) UNIQUE NOT NULL,
    phone varchar(20) NOT NULL,
    password TEXT NOT NULL,
    role user_role DEFAULT 'CITIZEN',
    birth_date DATE NOT NULL,
    accepted_terms BOOLEAN DEFAULT FALSE,
    accepted_privacy_policy BOOLEAN DEFAULT FALSE,
    terms_accepted_at TIMESTAMP,
    profile_photo_url TEXT,
    municipality_id INTEGER NOT NULL REFERENCES municipality(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE password_reset_token (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issue_report (
    id SERIAL PRIMARY KEY,
    municipality_id INTEGER NOT NULL REFERENCES municipality(id),
    category_id INTEGER NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    author_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    councilor_id INTEGER REFERENCES user_model(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    neighborhood VARCHAR(255),
    street VARCHAR(255),
    number VARCHAR(50),
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    status issue_status NOT NULL DEFAULT 'OPEN',
    moderation_status moderation_status NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (latitude BETWEEN -90 AND 90),
    CHECK (longitude BETWEEN -180 AND 180)
);

CREATE TABLE issue_image (
    id SERIAL PRIMARY KEY,
    issue_id INTEGER NOT NULL REFERENCES issue_report(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issue_status_history (
    id SERIAL PRIMARY KEY,
    issue_id INTEGER NOT NULL REFERENCES issue_report(id) ON DELETE CASCADE,
    previous_status issue_status,
    new_status issue_status NOT NULL,
    changed_by INTEGER REFERENCES user_model(id) ON DELETE SET NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issue_moderation (
    id SERIAL PRIMARY KEY,
    issue_id INTEGER NOT NULL REFERENCES issue_report(id) ON DELETE CASCADE,
    moderator_id INTEGER REFERENCES user_model(id) ON DELETE SET NULL,
    action moderation_action NOT NULL,
    feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE project (
                         id SERIAL PRIMARY KEY,
                         municipality_id INTEGER NOT NULL REFERENCES municipality(id) ON DELETE CASCADE,
                         category_id INTEGER NOT NULL REFERENCES category(id) ON DELETE CASCADE,
                         type project_type NOT NULL,
                         nature project_nature NOT NULL,
                         title VARCHAR(255) NOT NULL,
                         description TEXT,
                         status project_status DEFAULT 'PENDING_APPROVAL',
                         author_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
                         is_official BOOLEAN DEFAULT FALSE,
                         highlighted BOOLEAN DEFAULT FALSE,
                         neighborhood VARCHAR(255),
                         street VARCHAR(255),
                         number VARCHAR(50),
                         latitude DECIMAL(10,8),
                         longitude DECIMAL(11,8),
                         start_date DATE,
                         expected_end_date DATE,
                         end_date DATE,
                         financial_analysis TEXT,
                         estimated_cost NUMERIC(14,2),
                         approved_budget NUMERIC(14,2),
                         moderation_status moderation_status DEFAULT 'PENDING',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         CHECK (end_date IS NULL OR end_date >= start_date),
                         CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
                         CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
                         CHECK (nature <> 'PUBLIC_WORK' OR (latitude IS NOT NULL AND longitude IS NOT NULL))
);

CREATE TABLE project_image (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE project_councilor (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    councilor_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,

    UNIQUE(project_id, councilor_id)
);

CREATE TABLE project_opinion (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    opinion vote_type NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(project_id, user_id)
);

CREATE TABLE project_moderation (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    moderator_id INTEGER REFERENCES user_model(id) ON DELETE SET NULL,
    action moderation_action NOT NULL,
    feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE project_status_history (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    previous_status project_status,
    new_status project_status NOT NULL,
    changed_by INTEGER REFERENCES user_model(id) ON DELETE SET NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscription (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    type subscription_type NOT NULL,
    project_id INTEGER REFERENCES project(id) ON DELETE CASCADE,
    issue_id INTEGER REFERENCES issue_report(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES category(id) ON DELETE CASCADE,
    councilor_id INTEGER REFERENCES user_model(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type notification_type NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- Salas de conferência / audiências públicas
-- =============================================

CREATE TYPE room_status AS ENUM (
    'OPEN',
    'CLOSED'
);

CREATE TYPE participant_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'REMOVED'
);

CREATE TYPE speech_request_status AS ENUM (
    'NOT_REQUESTED',
    'PENDING',
    'APPROVED',
    'REJECTED'
);

CREATE TABLE conference_room (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    moderator_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    municipality_id INTEGER NOT NULL REFERENCES municipality(id) ON DELETE CASCADE,
    status room_status NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE room_participant (
    id SERIAL PRIMARY KEY,
    room_id INTEGER NOT NULL REFERENCES conference_room(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    status participant_status NOT NULL DEFAULT 'PENDING',
    speech_request_status speech_request_status NOT NULL DEFAULT 'NOT_REQUESTED',
    can_publish_audio BOOLEAN NOT NULL DEFAULT FALSE,
    can_publish_video BOOLEAN NOT NULL DEFAULT FALSE,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    decided_at TIMESTAMP,
    speech_requested_at TIMESTAMP,
    speech_decided_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- Assinaturas de projetos comunitários
-- =============================================

CREATE TABLE project_signature (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(project_id, user_id)
);

-- =============================================
-- Personalização / Acessibilidade do usuário
-- =============================================

CREATE TABLE user_settings (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE UNIQUE,
    font_size INTEGER NOT NULL DEFAULT 16
        CHECK (font_size BETWEEN 15 AND 30),
    accessibility_mode accessibility_mode NOT NULL DEFAULT 'NONE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- Log de auditoria de chamadas (mutações + login)
-- =============================================
-- Registra ações de escrita (POST/PUT/PATCH/DELETE) e autenticação.
-- GETs e Swagger não são registrados. Não guarda corpo da requisição.
-- user_id fica NULL em chamadas não autenticadas (ex.: login, forgot-password).

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES user_model(id) ON DELETE SET NULL,
    user_role VARCHAR(20),
    municipality_id INTEGER,
    http_method VARCHAR(10) NOT NULL,
    path VARCHAR(512) NOT NULL,
    query_string VARCHAR(1024),
    status_code INTEGER NOT NULL,
    success BOOLEAN NOT NULL,
    duration_ms BIGINT,
    ip_address VARCHAR(64),
    user_agent VARCHAR(512),
    error_message VARCHAR(1024),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);
CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_municipality ON audit_log(municipality_id);

-- =============================================
-- Eventos (publicação aberta, sem limite de município)
-- =============================================
-- Eventos são visíveis por qualquer usuário, de qualquer município.
-- Apenas MODERATOR/ADMINISTRATOR criam/editam/removem (sem moderação).
-- municipality_id é guardado apenas para permitir o filtro por município.

CREATE TABLE event_category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE event (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category_id INTEGER NOT NULL REFERENCES event_category(id),
    price NUMERIC(14,2),
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    neighborhood VARCHAR(255),
    street VARCHAR(255),
    number VARCHAR(50),
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    municipality_id INTEGER REFERENCES municipality(id) ON DELETE SET NULL,
    author_id INTEGER REFERENCES user_model(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date),
    CHECK (price IS NULL OR price >= 0)
);

CREATE TABLE event_image (
    id SERIAL PRIMARY KEY,
    event_id INTEGER NOT NULL REFERENCES event(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_event_category ON event(category_id);
CREATE INDEX idx_event_municipality ON event(municipality_id);
CREATE INDEX idx_event_start_date ON event(start_date);
CREATE INDEX idx_event_image_event ON event_image(event_id);
