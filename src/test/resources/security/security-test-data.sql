-- users
INSERT INTO app_user (id, username, display_name, password_hash, role, preferred_locale, enabled, created_at, version) VALUES
    (9001, 'admin', 'Admin', 'xyz', 'ADMIN', 'hr', true, now(), 0),
    (9002, 'lead1', 'Lead One', 'xyz', 'PROJECT_LEAD', 'hr', true, now(), 0),
    (9003, 'lead2', 'Lead Two', 'xyz', 'PROJECT_LEAD', 'hr', true, now(), 0),
    (9004, 'member1', 'Member', 'xyz', 'MEMBER', 'hr', true, now(), 0);

-- projects
INSERT INTO project (id, name, description, status, start_date, due_date, lead_id, created_at, version) VALUES
    (1, 'Project one', 2, 0),
    (2, 'Project two', 3, 0);

-- project member
INSERT INTO project_member (id, project_id, user_id) VALUES
    (123, 1, 4);

-- tasks
INSERT INTO task (id, project_id, title, description, status, estimate_days, start_date, due_date, completed_at, assignee_id, created_at, version) VALUES
    (9201, 9101, 'Assigned task',     'Assigned to member1',     'TODO', 3, DATE '2026-11-02', DATE '2026-11-04', NULL, 9004, now(), 0),
    (9202, 9101, 'Unassigned task',   'Nobody is assigned',      'TODO', 2, DATE '2026-11-05', DATE '2026-11-06', NULL, NULL, now(), 0),
    (9203, 9102, 'Other project task','Belongs to lead2''s project', 'TODO', 1, DATE '2026-11-02', DATE '2026-11-02', NULL, NULL, now(), 0);

