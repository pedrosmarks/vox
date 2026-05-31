CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS project_status_history CASCADE;
DROP TABLE IF EXISTS project_moderation CASCADE;
DROP TABLE IF EXISTS project_opinion CASCADE;
DROP TABLE IF EXISTS project_councilor CASCADE;
DROP TABLE IF EXISTS project_image CASCADE;
DROP TABLE IF EXISTS project CASCADE;
DROP TABLE IF EXISTS issue_image CASCADE;
DROP TABLE IF EXISTS issue_report CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS councilor_follow CASCADE;
DROP TABLE IF EXISTS password_reset_token CASCADE;
DROP TABLE IF EXISTS user_model CASCADE;
DROP TABLE IF EXISTS municipality CASCADE;

DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS project_type CASCADE;
DROP TYPE IF EXISTS project_status CASCADE;
DROP TYPE IF EXISTS moderation_action CASCADE;
DROP TYPE IF EXISTS issue_status CASCADE;
DROP TYPE IF EXISTS notification_type CASCADE;
DROP TYPE IF EXISTS vote_type CASCADE;

CREATE TYPE user_role AS ENUM (
    'CITIZEN',
    'COUNCILOR',
    'MODERATOR',
    'ADMINISTRATOR'
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

CREATE TYPE issue_status AS ENUM (
    'OPEN',
    'UNDER_REVIEW',
    'FORWARDED',
    'RESOLVED',
    'REJECTED'
);

CREATE TYPE notification_type AS ENUM (
    'PROJECT_CREATED',
    'PROJECT_TAGGED',
    'ISSUE_TAGGED',
    'ISSUE_STATUS_CHANGED',
    'PROJECT_STATUS_CHANGED'
);

CREATE TYPE vote_type AS ENUM (
    'APPROVE',
    'DISAPPROVE',
    'NEUTRAL'
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
    municipality_id INTEGER NOT NULL REFERENCES municipality(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE password_reset_token (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    token TEXT NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE councilor_follow (
    id SERIAL PRIMARY KEY,
    citizen_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    councilor_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE(citizen_id, councilor_id)
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
    author_id INTEGER NOT NULL REFERENCES user_model(id),
    councilor_id INTEGER NOT NULL REFERENCES user_model(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    neighborhood TEXT,
    street TEXT,
    number TEXT,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    status issue_status DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE issue_image (
    id SERIAL PRIMARY KEY,
    issue_id INTEGER NOT NULL REFERENCES issue_report(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE project (
    id SERIAL PRIMARY KEY,
    municipality_id INTEGER NOT NULL REFERENCES municipality(id) ON DELETE CASCADE,
    category_id INTEGER NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    type project_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status project_status DEFAULT 'PENDING_APPROVAL',
    author_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    highlighted BOOLEAN DEFAULT FALSE,
    is_official BOOLEAN DEFAULT FALSE,
    neighborhood TEXT,
    street TEXT,
    number TEXT,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    start_date DATE,
    expected_end_date DATE,
    end_date DATE,
    financial_analysis TEXT,
    estimated_cost NUMERIC(14,2),
    approved_budget NUMERIC(14,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CHECK (end_date IS NULL OR end_date >= start_date),
    CHECK (latitude BETWEEN -90 AND 90 OR latitude IS NULL),
    CHECK (longitude BETWEEN -180 AND 180 OR longitude IS NULL)
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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

CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type notification_type NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX IF NOT EXISTS idx_projects_status_municipality ON project(status, municipality_id);
CREATE INDEX IF NOT EXISTS idx_projects_type ON project(type);
CREATE INDEX IF NOT EXISTS idx_votes_project ON project_opinion(project_id);
CREATE INDEX IF NOT EXISTS idx_votes_user ON project_opinion(user_id);
CREATE INDEX IF NOT EXISTS idx_status_history_project ON project_status_history(project_id);
CREATE INDEX IF NOT EXISTS idx_project_status ON project(status);
CREATE INDEX IF NOT EXISTS idx_project_author ON project(author_id);
