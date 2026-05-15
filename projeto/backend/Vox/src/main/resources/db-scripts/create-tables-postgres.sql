CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS project_status_history;
DROP TABLE IF EXISTS project_moderation;
DROP TABLE IF EXISTS project_vote;
DROP TABLE IF EXISTS project_image;
DROP TABLE IF EXISTS reward;
DROP TABLE IF EXISTS project_model;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS user_model;
DROP TABLE IF EXISTS municipality;

DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS project_type CASCADE;
DROP TYPE IF EXISTS project_status CASCADE;
DROP TYPE IF EXISTS moderation_action CASCADE;

CREATE TYPE user_role AS ENUM (
    'citizen',
    'moderator',
    'admin'
);

CREATE TYPE project_type AS ENUM (
    'citizen',
    'chamber'
);

CREATE TYPE project_status AS ENUM (
    'pending_approval',
    'rejected',
    'published',
    'in_voting',
    'selected_by_council',
    'approved_by_council',
    'in_execution',
    'completed',
    'archived',
    'cancelled'
);

CREATE TYPE moderation_action AS ENUM (
    'approved',
    'rejected'
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
    role user_role DEFAULT 'citizen',
    birth_date DATE NOT NULL,
    accepted_terms BOOLEAN DEFAULT FALSE,
    accepted_privacy_policy BOOLEAN DEFAULT FALSE,
    terms_accepted_at TIMESTAMP,
    municipality_id INTEGER NOT NULL REFERENCES municipality(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE project_model (
    id SERIAL PRIMARY KEY,
    municipality_id INTEGER NOT NULL REFERENCES municipality(id) ON DELETE CASCADE,
    category_id INTEGER NOT NULL REFERENCES category(id) ON DELETE CASCADE,
    type project_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status project_status DEFAULT 'pending_approval',
    author_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
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

    CHECK (end_date IS NULL OR end_date >= start_date),
    CHECK (latitude BETWEEN -90 AND 90 OR latitude IS NULL),
    CHECK (longitude BETWEEN -180 AND 180 OR longitude IS NULL)
);

CREATE TABLE project_image (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES project_model(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE project_vote (
    id SERIAL PRIMARY KEY,
    voting_start_at TIMESTAMP,
    voting_end_at TIMESTAMP,
    project_id INTEGER NOT NULL REFERENCES project_model(id) ON DELETE CASCADE,
    user_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (project_id, user_id)
);

CREATE TABLE project_moderation (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES project_model(id) ON DELETE CASCADE,
    moderator_id INTEGER REFERENCES user_model(id) ON DELETE SET NULL,
    action moderation_action NOT NULL,
    feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE project_status_history (
    id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL REFERENCES project_model(id) ON DELETE CASCADE,
    previous_status project_status,
    new_status project_status NOT NULL,
    changed_by INTEGER REFERENCES user_model(id) ON DELETE SET NULL,
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reward (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES user_model(id) ON DELETE CASCADE,
    project_id INTEGER NOT NULL REFERENCES project_model(id) ON DELETE CASCADE,
    type VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_projects_status_municipality ON project_model(status, municipality_id);
CREATE INDEX idx_projects_type ON project_model(type);
CREATE INDEX idx_votes_project ON project_vote(project_id);
CREATE INDEX idx_votes_user ON project_vote(user_id);
CREATE INDEX idx_status_history_project ON project_status_history(project_id);
CREATE INDEX idx_project_status ON project_model(status);
CREATE INDEX idx_project_author ON project_model(author_id);