-- =========================================================================================
-- SEED DATA
-- =========================================================================================

-- password is the same for all demo account: Demo1234!
-- (BCrypt hash below). The accounts are listed in the README

-- Users
INSERT INTO app_user (id, username, display_name, password_hash, role, preferred_locale) VALUES
    (1, 'admin', 'Ivona Admin', '$2a$10$TTebGdf3JbrTjDHpfXZPxO6bdaR9UHOIEJfKyIHThet87llM31U4e', 'ADMIN', 'hr'),
    (2, 'pperic', 'Pero Peric', '$2a$10$TTebGdf3JbrTjDHpfXZPxO6bdaR9UHOIEJfKyIHThet87llM31U4e', 'PROJECT_LEAD', 'en'),
    (3, 'iivic', 'Ivo Ivic', '$2a$10$TTebGdf3JbrTjDHpfXZPxO6bdaR9UHOIEJfKyIHThet87llM31U4e', 'PROJECT_LEAD', 'en'),
    (4, 'aanic', 'Ana Anic', '$2a$10$TTebGdf3JbrTjDHpfXZPxO6bdaR9UHOIEJfKyIHThet87llM31U4e', 'PROJECT_LEAD', 'en'),
    (5, 'hhrvic', 'Hrvoje Hrvic', '$2a$10$TTebGdf3JbrTjDHpfXZPxO6bdaR9UHOIEJfKyIHThet87llM31U4e', 'PROJECT_LEAD', 'en'),
    (6, 'mmaric', 'Marija Maric', '$2a$10$TTebGdf3JbrTjDHpfXZPxO6bdaR9UHOIEJfKyIHThet87llM31U4e', 'PROJECT_LEAD', 'en'),
    (7, 'shorvat', 'Stjepan Horvat', '$2a$10$TTebGdf3JbrTjDHpfXZPxO6bdaR9UHOIEJfKyIHThet87llM31U4e', 'PROJECT_LEAD', 'en'),
    (8, 'llukic', 'Luka Lukic', '$2a$10$TTebGdf3JbrTjDHpfXZPxO6bdaR9UHOIEJfKyIHThet87llM31U4e', 'PROJECT_LEAD', 'en'),
    (9, 'ihorvat', 'Ivana Horvat', '$2a$10$TTebGdf3JbrTjDHpfXZPxO6bdaR9UHOIEJfKyIHThet87llM31U4e', 'PROJECT_LEAD', 'en'),
    (10, 'ahorvat', 'Ana Horvat', '$2a$10$TTebGdf3JbrTjDHpfXZPxO6bdaR9UHOIEJfKyIHThet87llM31U4e', 'MEMBER', 'hr'),
    (11, 'pkovac', 'Petra Kovac', '$2a$10$TTebGdf3JbrTjDHpfXZPxO6bdaR9UHOIEJfKyIHThet87llM31U4e', 'MEMBER', 'hr'),
    (12, 'ctima', 'Clan Tima', '$2a$10$TTebGdf3JbrTjDHpfXZPxO6bdaR9UHOIEJfKyIHThet87llM31U4e', 'MEMBER', 'hr');

-- Projects
INSERT INTO project (id, name, description, status, start_date, due_date, lead_id)  VALUES
    (1, 'Website Redesign', 'New public website: structure, visual design, CMS and launch.', 'IN_PROGRESS', '2026-10-01', '2026-12-15', 2),
    (2, 'Mobile App v2', 'Customer mobile app version 2, improved.', 'BLOCKED','2026-09-15', '2026-12-15', 4),
    (3, 'Q3 Marketing Campaign', 'Autumn campaign across social and search.', 'IN_PROGRESS', '2026-09-01', '2026-10-12', 3),
    (4, 'HR System Integration', 'Connect the HR system with payroll.', 'DONE', '2026-08-01', '2026-09-28', 7),
    (5, 'Sales Analytics Dashboard', 'Weekly sales KPIs for the management team.', 'IN_PROGRESS', '2026-09-10', '2026-10-20', 5),
    (6, 'Customer Portal Update', 'Refresh of the customer self-service portal.', 'TODO', '2026-10-20', '2026-11-05', 6),
    (7, 'Security Audit Q4', 'External penetration test and fixes.', 'DONE', '2026-09-01', '2026-10-10', 8),
    (8, 'Employee Onboarding Flow', 'Checklist and portal for new employees.', 'IN_PROGRESS', '2026-10-01', '2026-11-15', 9);

-- Project members
INSERT INTO project_member (id, project_id, user_id, allocation_percent) VALUES
    (1, 1, 2, 50),
    (2, 1, 3, 80),
    (3, 1, 4, 50),
    (4, 1, 5, 60),
    (5, 1, 6, 100),
    (6, 1, 7, 70),
    (7, 1, 8, 50),
    (8, 1, 9, 40),
    (9, 1, 10, 100),
    (10, 2, 4, 50),
    (11, 2, 11, 100),
    (12, 2, 12, 100),
    (13, 2, 6, 100),
    (14, 3, 3, 20),
    (15, 3, 5, 40),
    (16, 3, 9, 30),
    (17, 4, 7, 30),
    (18, 5, 5, 40),
    (19, 5, 9, 30),
    (20, 6, 6, 50),
    (21, 7, 8, 50),
    (22, 7, 7, 30),
    (23, 8, 9, 30),
    (24, 8, 10, 50);

-- Tasks
-- Website Redesign (1): 20 tasks, 8 done, critical path 32 working days:
-- 101 -> 102 -> 104 -> 105 -> 106 -> 107 -> 108  (3+4+6+8+5+4+2)
INSERT INTO task (id, project_id, title, status, estimate_days, assignee_id, completed_at) VALUES
    (101, 1, 'Research & content audit', 'DONE', 3, 3, '2026-10-05 16:00:00+02'),
    (102, 1, 'Information architecture', 'DONE', 4, 2, '2026-10-09 16:00:00+02'),
    (103, 1, 'Homepage wireframes', 'IN_PROGRESS', 5, 4, null),
    (104, 1, 'Visual design system', 'IN_PROGRESS', 6, 5, null),
    (105, 1, 'Homepage development', 'BLOCKED', 8, 6, null),
    (106, 1, 'CMS integration', 'TODO', 5, 7, null),
    (107, 1, 'Accessibility & QA testing', 'TODO', 4, 8, null),
    (108, 1, 'Launch & handover', 'TODO', 2, 2, null),
    (109, 1, 'Stakeholder interviews', 'DONE', 2, 3, '2026-10-02 16:00:00+02'),
    (110, 1, 'Competitor analysis', 'DONE', 2, 5, '2026-10-06 16:00:00+02'),
    (111, 1, 'Analytics baseline report', 'DONE', 1, 10, '2026-10-02 16:00:00+02'),
    (112, 1, 'Brand guidelines review', 'DONE', 2, 5, '2026-10-07 16:00:00+02'),
    (113, 1, 'Content inventory spreadsheet', 'DONE', 2, 3, '2026-10-08 16:00:00+02'),
    (114, 1, 'Hosting environment setup', 'DONE', 3, 7, '2026-10-12 16:00:00+02'),
    (115, 1, 'Copywriting for key pages', 'IN_PROGRESS', 5, 3, null),
    (116, 1, 'Image and icon library', 'IN_PROGRESS', 3, 5, null),
    (117, 1, 'Content migration', 'TODO', 4, 7, null),
    (118, 1, 'SEO redirect map', 'TODO', 2, 10, null),
    (119, 1, 'Performance budget checks', 'TODO', 2, 6, null),
    (120, 1, 'Training for content editors', 'TODO', 1, 10, null),
    (201, 2, 'Requirements refresh', 'DONE', 3, 4, '2026-09-18 16:00:00+02'),
    (202, 2, 'API contract v2', 'DONE', 4, 11, '2026-09-25 16:00:00+02'),
    (203, 2, 'Auth flow redesign', 'IN_PROGRESS', 5, 12, null),
    (204, 2, 'Offline sync', 'BLOCKED', 8, 11, null),
    (205, 2, 'Push notifications', 'TODO', 4, 6, null),
    (206, 2, 'New onboarding screens', 'IN_PROGRESS', 5, 12, null),
    (207, 2, 'Payments SDK upgrade', 'BLOCKED', 3, 6, null),
    (208, 2, 'Integration testing', 'TODO', 5, 11, null),
    (209, 2, 'Accessibility review', 'TODO', 2, 12, null),
    (210, 2, 'Beta release', 'TODO', 2, 4, null),
    (211, 2, 'Store listing update', 'TODO', 1, 4, null),
    (212, 2, 'Public release', 'TODO', 1, 4, null),
    (301, 3, 'Campaign brief', 'DONE', 2, 3, '2026-09-04 16:00:00+02'),
    (302, 3, 'Ad creatives', 'IN_PROGRESS', 4, 5, null),
    (303, 3, 'Results report', 'TODO', 2, 9, null),
    (304, 4, 'Data mapping', 'DONE', 3, 7, '2026-08-20 16:00:00+02'),
    (305, 4, 'Payroll export', 'DONE', 4, 7, '2026-09-25 16:00:00+02'),
    (306, 5, 'KPI definitions', 'DONE', 2, 5, '2026-09-15 16:00:00+02'),
    (307, 5, 'Dashboard build', 'IN_PROGRESS', 5, 9, null),
    (308, 6, 'Scope workshop', 'TODO', 1, 6, null),
    (309, 6, 'UI refresh', 'TODO', 6, 6, null),
    (310, 7, 'Penetration test', 'DONE', 4, 8, '2026-09-20 16:00:00+02'),
    (311, 7, 'Fix findings', 'DONE', 3, 7, '2026-10-08 16:00:00+02'),
    (312, 8, 'Onboarding checklist draft', 'DONE', 2, 9, '2026-10-06 16:00:00+02'),
    (313, 8, 'Welcome portal page', 'IN_PROGRESS', 3, 10, null),
    (314, 8, 'Buddy assignment', 'TODO', 1, 10, null);

-- Subtask
INSERT INTO subtask (id, task_id, title, done, position) VALUES
    (1, 103, 'Desktop layout', true, 0),
    (2, 103, 'Mobile layout', false, 1),
    (3, 105, 'Hero section', false, 0),
    (4, 105, 'Navigation', false, 1),
    (5, 105, 'Footer', false, 2),
    (6, 107, 'Keyboard navigation check', false, 0),
    (7, 107, 'Screen reader pass', false, 1),
    (8, 107, 'Colour contrast check', false, 2),
    (9, 115, 'About us page', true, 0),
    (10, 115, 'Services page', false, 1);

-- Task dependency
INSERT INTO task_dependency (id, project_id, predecessor_id, successor_id) VALUES
    (1, 1, 101, 102),
    (2, 1, 102, 103),
    (3, 1, 102, 104),
    (4, 1, 103, 105),
    (5, 1, 104, 105),
    (6, 1, 105, 106),
    (7, 1, 106, 107),
    (8, 1, 107, 108),
    (9, 1, 109, 110),
    (10, 1, 101, 113),
    (11, 1, 102, 115),
    (12, 1, 104, 116),
    (13, 1, 106, 117),
    (14, 1, 115, 117),
    (15, 1, 113, 118),
    (16, 1, 105, 119),
    (17, 1, 117, 120),
    (18, 2, 201, 202),
    (19, 2, 202, 203),
    (20, 2, 202, 204),
    (21, 2, 202, 205),
    (22, 2, 201, 206),
    (23, 2, 203, 208),
    (24, 2, 204, 208),
    (25, 2, 205, 208),
    (26, 2, 206, 209),
    (27, 2, 207, 210),
    (28, 2, 208, 210),
    (29, 2, 209, 210),
    (30, 2, 206, 211),
    (31, 2, 210, 212),
    (32, 2, 211, 212),
    (33, 3, 301, 302),
    (34, 3, 302, 303),
    (35, 4, 304, 305),
    (36, 5, 306, 307),
    (37, 6, 308, 309),
    (38, 7, 310, 311),
    (39, 8, 312, 313);

-- AI feature: one analysed note with the suggestions shown in the prototype
INSERT INTO meeting_note (id, project_id, created_by, raw_text, status, created_at, processed_at) VALUES
    (1, 1, 2, 'Website redesign kickoff - 12 Oct 2026
    - Homepage hero needs a stronger CTA and clearer hierarchy. Michael asked for a wireframe review before design handoff.
    - Content audit is mostly complete, but legal still needs to approve the footer copy.
    - Accessibility testing should run in parallel with QA once the CMS integration is stable.
    - Launch date is still 15 Dec.
    - David confirmed CMS integration can start as soon as the content structure is locked.

    Action items:
    - Homepage wireframes (Emily)
    - Content audit sign-off (Sarah)
    - Accessibility testing (Marko from the agency)
    - CMS integration (David)', 'DONE', '2026-10-12 10:00:00+02', '2026-10-12 10:00:20+02');

-- Task suggestion
INSERT INTO task_suggestion (id, note_id, title, suggested_owner_name, matched_user_id, estimate_days,
                             source_quote, status, created_task_id, decided_by, decided_at) VALUES
    (1, 1, 'Homepage wireframes', 'Emily', 4, 5, 'Homepage hero needs a stronger CTA and clearer hierarchy.', 'ACCEPTED', 103, 2, '2026-10-12 10:05:00+02'),
    (2, 1, 'Content audit sign-off', 'Sarah', 3, 3, 'Content audit is mostly complete, but legal still needs to approve the footer copy.', 'PENDING', null, null, null),
    (3, 1, 'Accessibility testing', 'Marko', null, 4, 'Accessibility testing should run in parallel with QA once the CMS integration is stable.', 'PENDING', null, null, null),
    (4, 1, 'CMS integration', 'David', 7, 5, 'David confirmed CMS integration can start as soon as the content structure is locked.', 'PENDING', null, null, null);

-- Burndown history for Website Redesign (remaining + completed = 66 days)
INSERT INTO burndown_snapshot (id, project_id, snapshot_date, remaining_days, completed_days) VALUES
    (1, 1, '2026-10-01', 66, 0),
    (2, 1, '2026-10-02', 63, 3),
    (3, 1, '2026-10-03', 63, 3),
    (4, 1, '2026-10-04', 63, 3),
    (5, 1, '2026-10-05', 60, 6),
    (6, 1, '2026-10-06', 58, 8),
    (7, 1, '2026-10-07', 56, 10),
    (8, 1, '2026-10-08', 54, 12),
    (9, 1, '2026-10-09', 50, 16),
    (10, 1, '2026-10-10', 50, 16),
    (11, 1, '2026-10-11', 50, 16),
    (12, 1, '2026-10-12', 47, 19),
    (13, 1, '2026-10-13', 47, 19),
    (14, 1, '2026-10-14', 47, 19),
    (15, 1, '2026-10-15', 47, 19),
    (16, 1, '2026-10-16', 47, 19);

-- Move every sequence past the seeded ids. Hibernate's pooled optimizer takes the block (value - 49 ... value), so the next value must be at least max(id) + 50.
select setval('app_user_seq', (select coalesce(max(id), 0) + 50 from app_user));
select setval('project_seq', (select coalesce(max(id), 0) + 50 from project));
select setval('project_member_seq', (select coalesce(max(id), 0) + 50 from project_member));
select setval('task_seq', (select coalesce(max(id), 0) + 50 from task));
select setval('subtask_seq', (select coalesce(max(id), 0) + 50 from subtask));
select setval('    task_dependency_seq', (select coalesce(max(id), 0) + 50 from task_dependency));
select setval('meeting_note_seq', (select coalesce(max(id), 0) + 50 from meeting_note));
select setval('task_suggestion_seq', (select coalesce(max(id), 0) + 50 from task_suggestion));
select setval('burndown_snapshot_seq', (select coalesce(max(id), 0) + 50 from burndown_snapshot));
