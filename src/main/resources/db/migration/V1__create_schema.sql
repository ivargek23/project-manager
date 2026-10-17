-- =========================================================================================
-- DATABASE SCHEMA
-- =========================================================================================

-- USER
CREATE SEQUENCE app_user_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE app_user (
id                  BIGINT          PRIMARY KEY,
username            VARCHAR(50)     NOT NULL UNIQUE,
display_name        VARCHAR(100)    NOT NULL,
password_hash       VARCHAR(100)    NOT NULL,
role                VARCHAR(20)     NOT NULL,
preferred_locale    VARCHAR(5)      NOT NULL DEFAULT 'hr',
enabled             BOOLEAN         NOT NULL DEFAULT TRUE,
created_at          timestamptz     NOT NULL DEFAULT NOW(),
version             BIGINT          NOT NULL DEFAULT 0,

CONSTRAINT check_app_user_role
    CHECK (role IN ('ADMIN', 'PROJECT_LEAD', 'MEMBER')),

CONSTRAINT check_app_user_locale
    CHECK (preferred_locale IN ('hr', 'en'))
);

-- PROJECT
CREATE SEQUENCE project_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE project (
id          BIGINT          PRIMARY KEY,
name        VARCHAR(150)    NOT NULL,
description TEXT,
status      VARCHAR(20)     NOT NULL DEFAULT 'TODO',
start_date  DATE            NOT NULL,
due_date    DATE            NOT NULL,
lead_id     BIGINT          NOT NULL REFERENCES app_user (id),
created_at  timestamptz     NOT NULL DEFAULT NOW(),
version     BIGINT          NOT NULL DEFAULT 0,

CONSTRAINT check_project_status
 CHECK (status in ('TODO', 'IN_PROGRESS', 'BLOCKED', 'DONE')),

CONSTRAINT check_project_dates CHECK (due_date >= start_date)
);

CREATE INDEX project_lead_idx ON project (lead_id);

-- Many-to-many Project <-> AppUser with attributes (association entity)
CREATE SEQUENCE project_member_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE project_member (
id                 BIGINT      PRIMARY KEY,
project_id         BIGINT      NOT NULL,
user_id            BIGINT      NOT NULL,
allocation_percent int         NOT NULL DEFAULT 100,
joined_at          timestamptz NOT NULL DEFAULT now(),

CONSTRAINT fk_project_member_project
    FOREIGN KEY (project_id)
        REFERENCES project(id)
        ON DELETE CASCADE,

CONSTRAINT fk_project_member_user
    FOREIGN KEY (user_id)
        REFERENCES app_user(id),

CONSTRAINT chk_project_member_allocation
    CHECK (allocation_percent BETWEEN 1 AND 100),

CONSTRAINT uq_project_member_project_user UNIQUE (project_id, user_id)
);

CREATE INDEX project_member_user_idx ON project_member (user_id);

-- Task
CREATE SEQUENCE task_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE task (
id            BIGINT       PRIMARY KEY,
project_id    BIGINT       NOT NULL REFERENCES project (id) on delete cascade,
title         VARCHAR(200) NOT NULL,
description   TEXT,
status        VARCHAR(20)  NOT NULL DEFAULT 'TODO',
estimate_days int          NOT NULL CHECK (estimate_days between 1 and 100),
start_date    DATE,
due_date      DATE,
completed_at  timestamptz,
assignee_id   BIGINT,
created_at    timestamptz  NOT NULL DEFAULT NOW(),
version       BIGINT       NOT NULL DEFAULT 0,

CONSTRAINT fk_task_project
  FOREIGN KEY (project_id)
      REFERENCES project(id)
      ON DELETE CASCADE,

CONSTRAINT fk_task_assignee
  FOREIGN KEY (assignee_id)
      REFERENCES app_user(id)
      ON DELETE SET NULL,

CONSTRAINT chk_task_status
  CHECK (status IN ('TODO', 'IN_PROGRESS', 'BLOCKED', 'DONE')),

CONSTRAINT chk_task_estimate
  CHECK (estimate_days BETWEEN 1 AND 100),

CONSTRAINT chk_task_dates
  CHECK (due_date IS NULL OR start_date IS NULL OR due_date >= start_date),

-- Prevent dependencies between tasks from different projects.
CONSTRAINT uq_task_id_project
  UNIQUE (id, project_id)
);

CREATE INDEX idx_task_project_status
    ON task (project_id, status);

CREATE INDEX idx_task_assignee
    ON task (assignee_id);

-- Subtask

CREATE SEQUENCE subtask_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE subtask (
id          BIGINT       PRIMARY KEY,
task_id     BIGINT       NOT NULL,
title       VARCHAR(200) NOT NULL,
done        BOOLEAN      NOT NULL DEFAULT FALSE,
position    INT          NOT NULL DEFAULT 0,
created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
version     BIGINT       NOT NULL DEFAULT 0,

CONSTRAINT fk_subtask_task
 FOREIGN KEY (task_id)
     REFERENCES task(id)
     ON DELETE CASCADE
);

CREATE INDEX idx_subtask_task_position
    ON subtask (task_id, position);

-- Task dependencies: many-to-many Task <-> Task with attributes

CREATE SEQUENCE task_dependency_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE task_dependency (
id              BIGINT       PRIMARY KEY,
project_id      BIGINT       NOT NULL,
predecessor_id  BIGINT       NOT NULL,
successor_id    BIGINT       NOT NULL,
type            VARCHAR(20)  NOT NULL DEFAULT 'FINISH_TO_START',
lag_days        INT          NOT NULL DEFAULT 0,
created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

CONSTRAINT fk_task_dependency_predecessor
 FOREIGN KEY (predecessor_id, project_id)
     REFERENCES task(id, project_id)
     ON DELETE CASCADE,

CONSTRAINT fk_task_dependency_successor
 FOREIGN KEY (successor_id, project_id)
     REFERENCES task(id, project_id)
     ON DELETE CASCADE,

CONSTRAINT chk_task_dependency_type
 CHECK (type IN ('FINISH_TO_START')),

CONSTRAINT chk_task_dependency_lag
 CHECK (lag_days BETWEEN 0 AND 100),

CONSTRAINT chk_task_dependency_self
 CHECK (predecessor_id <> successor_id),

CONSTRAINT uq_task_dependency_pair
 UNIQUE (predecessor_id, successor_id)
);

-- Indexes for project dependency graphs and successor lookups
CREATE INDEX idx_task_dependency_project_predecessor
    ON task_dependency (project_id, predecessor_id);

CREATE INDEX idx_task_dependency_successor
    ON task_dependency (successor_id);

-- AI feature: meeting notes and task suggestions

CREATE SEQUENCE meeting_note_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE meeting_note (
id             BIGINT       PRIMARY KEY,
project_id     BIGINT       NOT NULL,
created_by     BIGINT       NOT NULL,
raw_text       TEXT         NOT NULL,
status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
error_message  VARCHAR(500),
created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
processed_at   TIMESTAMPTZ,
version        BIGINT       NOT NULL DEFAULT 0,

CONSTRAINT fk_meeting_note_project
  FOREIGN KEY (project_id)
      REFERENCES project(id)
      ON DELETE CASCADE,

CONSTRAINT fk_meeting_note_creator
  FOREIGN KEY (created_by)
      REFERENCES app_user(id),

CONSTRAINT chk_meeting_note_raw_text
  CHECK (CHAR_LENGTH(raw_text) BETWEEN 1 AND 10000),

CONSTRAINT chk_meeting_note_status
  CHECK (status IN ('PENDING', 'PROCESSING', 'DONE', 'FAILED'))
);

CREATE INDEX idx_meeting_note_project
    ON meeting_note (project_id, created_at DESC);

-- Daily cap: count a user's notes since midnight
CREATE INDEX idx_meeting_note_user_day
    ON meeting_note (created_by, created_at);

-- Task suggestion
CREATE SEQUENCE task_suggestion_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE task_suggestion (
id                    BIGINT       PRIMARY KEY,
note_id               BIGINT       NOT NULL,
title                 VARCHAR(200) NOT NULL,
suggested_owner_name  VARCHAR(100),
matched_user_id       BIGINT,
estimate_days         INT,
source_quote          VARCHAR(1000),
status                VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
created_task_id       BIGINT       UNIQUE,
decided_by            BIGINT,
decided_at            TIMESTAMPTZ,
version               BIGINT       NOT NULL DEFAULT 0,

CONSTRAINT fk_task_suggestion_note
 FOREIGN KEY (note_id)
     REFERENCES meeting_note(id)
     ON DELETE CASCADE,

CONSTRAINT fk_task_suggestion_matched_user
 FOREIGN KEY (matched_user_id)
     REFERENCES app_user(id),

CONSTRAINT fk_task_suggestion_created_task
 FOREIGN KEY (created_task_id)
     REFERENCES task(id)
     ON DELETE SET NULL,

CONSTRAINT fk_task_suggestion_decided_by
 FOREIGN KEY (decided_by)
     REFERENCES app_user(id),

CONSTRAINT chk_task_suggestion_estimate
 CHECK (estimate_days BETWEEN 1 AND 100),

CONSTRAINT chk_task_suggestion_status
 CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),

CONSTRAINT chk_task_suggestion_decision
 CHECK (
     (status = 'PENDING' AND decided_at IS NULL)
         OR
     (status <> 'PENDING' AND decided_at IS NOT NULL AND decided_by IS NOT NULL)
     )
);

CREATE INDEX idx_task_suggestion_note_status
    ON task_suggestion (note_id, status);

-- Burndown history, written by the nightly job

CREATE SEQUENCE burndown_snapshot_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE burndown_snapshot (
id              BIGINT PRIMARY KEY,
project_id      BIGINT NOT NULL,
snapshot_date   DATE   NOT NULL,
remaining_days  INT    NOT NULL,
completed_days  INT    NOT NULL,

CONSTRAINT fk_burndown_snapshot_project
   FOREIGN KEY (project_id)
       REFERENCES project(id)
       ON DELETE CASCADE,

CONSTRAINT chk_burndown_snapshot_remaining
   CHECK (remaining_days >= 0),

CONSTRAINT chk_burndown_snapshot_completed
   CHECK (completed_days >= 0),

CONSTRAINT uq_burndown_snapshot_project_date
   UNIQUE (project_id, snapshot_date)
);