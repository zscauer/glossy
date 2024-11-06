-- Schema --

CREATE SCHEMA IF NOT EXISTS glsy;
SET SEARCH_PATH TO glsy;


-- Types --

CREATE TYPE language AS ENUM ('en', 'ru');
CREATE TYPE access_mode AS ENUM ('OBSERVER', 'MODERATOR', 'ADMIN');
CREATE TYPE accessible_content AS ENUM ('INFORMATION_NOTE', 'INFORMATION_NOTE_HISTORY', 'TAG');
CREATE TYPE privilege AS ENUM ('READ', 'UPDATE', 'CREATE', 'DELETE');
CREATE TYPE lifecycle_event AS ENUM ('CREATED', 'UPDATED', 'ACTIVATED', 'DEACTIVATED', 'DELETED');


-- Tables --

CREATE TABLE IF NOT EXISTS access_modes_content_privileges
(
    access_mode access_mode,
    accessible_content accessible_content,
    privilege privilege
);

COMMENT ON TABLE access_modes_content_privileges IS 'Content access privileges of access modes';
COMMENT ON COLUMN access_modes_content_privileges.access_mode IS 'Access mode';
COMMENT ON COLUMN access_modes_content_privileges.accessible_content IS 'Content';
COMMENT ON COLUMN access_modes_content_privileges.privilege IS 'Privilege';


CREATE TABLE IF NOT EXISTS tags
(
    id uuid,
    name varchar UNIQUE, -- INDEX ALWAYS CREATED BY H2 AUTOMATICALLY
    CONSTRAINT "tags_pk" PRIMARY KEY (id) -- INDEX ALWAYS CREATED BY H2 AUTOMATICALLY
);

COMMENT ON TABLE tags IS 'Tags (keywords) to link few records';
COMMENT ON COLUMN tags.id IS 'Id of tag';
COMMENT ON COLUMN tags.name IS 'Keyword';


CREATE TABLE IF NOT EXISTS information_notes
(
    id uuid,
    name varchar UNIQUE, -- INDEX ALWAYS CREATED BY H2 AUTOMATICALLY
    description varchar,
    actual boolean DEFAULT true,
    CONSTRAINT "information_notes_pk" PRIMARY KEY (id) -- INDEX ALWAYS CREATED BY H2 AUTOMATICALLY
);

COMMENT ON TABLE information_notes IS 'Records with information';
COMMENT ON COLUMN information_notes.id IS 'Id of information card';
COMMENT ON COLUMN information_notes.name IS 'Name (topic)';
COMMENT ON COLUMN information_notes.description IS 'Description';
COMMENT ON COLUMN information_notes.actual IS 'Current status of information in note (to hide, filter, etc)';


CREATE TABLE IF NOT EXISTS information_notes_tags -- Many to many relations of information_notes and tags tables
(
    information_note_id uuid NOT NULL,
    tag_id uuid NOT NULL,
    UNIQUE(information_note_id, tag_id) -- COMPOSITE INDEX ALWAYS CREATED BY H2 AUTOMATICALLY
);
CREATE INDEX IF NOT EXISTS "information_notes_tags_information_note_id_idx" ON information_notes_tags (information_note_id);
CREATE INDEX IF NOT EXISTS "information_notes_tags_tag_id_idx" ON information_notes_tags (tag_id);

COMMENT ON TABLE information_notes_tags IS 'Connections of information notes and their tags (keywords)';
COMMENT ON COLUMN information_notes_tags.information_note_id IS 'Id of information card';
COMMENT ON COLUMN information_notes_tags.tag_id IS 'Id of tag';


CREATE TABLE IF NOT EXISTS information_notes_history
(
    id bigint GENERATED ALWAYS AS IDENTITY,
    information_note_id uuid REFERENCES information_notes(id), -- INDEX ALWAYS CREATED BY H2 AUTOMATICALLY
    event lifecycle_event,
    event_time timestamp,
    author varchar,
    state text,
    CONSTRAINT "information_notes_history_pk" PRIMARY KEY (id) -- INDEX ALWAYS CREATED BY H2 AUTOMATICALLY
);

COMMENT ON TABLE information_notes_history IS 'History of changes of information notes (ONLY INSERTING SHOULD BE ALLOWED)';
COMMENT ON COLUMN information_notes_history.id IS 'Id of change entry';
COMMENT ON COLUMN information_notes_history.information_note_id IS 'Id of changed information note';
COMMENT ON COLUMN information_notes_history.event IS 'Type of change';
COMMENT ON COLUMN information_notes_history.event_time IS 'Time of change';
COMMENT ON COLUMN information_notes_history.author IS 'Author of change';
COMMENT ON COLUMN information_notes_history.state IS 'Saved state of information card';


-- Initial data --

INSERT INTO access_modes_content_privileges (access_mode, accessible_content, privilege) VALUES
('OBSERVER', 'INFORMATION_NOTE', 'READ'),
('OBSERVER', 'TAG', 'READ'),

('MODERATOR', 'INFORMATION_NOTE', 'CREATE'),
('MODERATOR', 'TAG', 'CREATE'),

('ADMIN', 'INFORMATION_NOTE', 'DELETE'),
('ADMIN', 'INFORMATION_NOTE_HISTORY', 'READ'),
('ADMIN', 'TAG', 'DELETE');