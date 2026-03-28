CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-----------------------------
-- ENUMS
-----------------------------

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

-----------------------------
-- MUNICIPALITIES
-----------------------------

CREATE TABLE municipalities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    state CHAR(2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name, state)
);

-----------------------------
-- USERS
-----------------------------

CREATE TABLE user_model (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password TEXT NOT NULL,

    role user_role DEFAULT 'citizen',

    municipality_id INTEGER NOT NULL
        REFERENCES municipalities(id) ON DELETE CASCADE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-----------------------------
-- PROJECTS
-----------------------------

CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    municipality_id INTEGER NOT NULL
        REFERENCES municipalities(id) ON DELETE CASCADE,

    type project_type NOT NULL,

    title VARCHAR(255) NOT NULL,
    description TEXT,

    status project_status DEFAULT 'pending_approval',

    author_id UUID
        REFERENCES user_model(id),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    highlighted BOOLEAN DEFAULT FALSE,
    is_official BOOLEAN DEFAULT FALSE,

    location_name TEXT,
    address TEXT,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),

    start_date DATE,
    expected_end_date DATE,
    end_date DATE,

    budget NUMERIC(14,2),

    CHECK (latitude BETWEEN -90 AND 90 OR latitude IS NULL),
    CHECK (longitude BETWEEN -180 AND 180 OR longitude IS NULL)
);

-----------------------------
-- PROJECT IMAGES
-----------------------------

CREATE TABLE project_images (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    project_id UUID NOT NULL
        REFERENCES projects(id) ON DELETE CASCADE,

    url TEXT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-----------------------------
-- VOTES
-----------------------------

CREATE TABLE project_votes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    project_id UUID NOT NULL
        REFERENCES projects(id) ON DELETE CASCADE,

    user_id UUID NOT NULL
        REFERENCES user_model(id) ON DELETE CASCADE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE (project_id, user_id)
);

-----------------------------
-- PROJECT MODERATION
-----------------------------

CREATE TABLE project_moderation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    project_id UUID NOT NULL
        REFERENCES projects(id) ON DELETE CASCADE,

    moderator_id UUID NOT NULL
        REFERENCES user_model(id),

    action moderation_action NOT NULL,

    feedback TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-----------------------------
-- PROJECT STATUS HISTORY
-----------------------------

CREATE TABLE project_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    project_id UUID NOT NULL
        REFERENCES projects(id) ON DELETE CASCADE,

    previous_status project_status,
    new_status project_status NOT NULL,

    changed_by UUID
        REFERENCES user_model(id),

    note TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-----------------------------
-- REWARDS
-----------------------------

CREATE TABLE rewards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    user_id UUID
        REFERENCES user_model(id),

    project_id UUID
        REFERENCES projects(id),

    type VARCHAR(50),

    description TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-----------------------------
-- INDEXES
-----------------------------

CREATE INDEX idx_projects_municipality
ON projects(municipality_id);

CREATE INDEX idx_projects_status
ON projects(status);

CREATE INDEX idx_projects_type
ON projects(type);

CREATE INDEX idx_votes_project
ON project_votes(project_id);

CREATE INDEX idx_votes_user
ON project_votes(user_id);

CREATE INDEX idx_status_history_project
ON project_status_history(project_id);