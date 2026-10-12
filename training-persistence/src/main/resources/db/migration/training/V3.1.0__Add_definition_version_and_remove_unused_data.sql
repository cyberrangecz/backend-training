DELETE FROM training_level_mitre_technique
WHERE mitre_technique_id IN (SELECT id
                             FROM mitre_technique
                             WHERE technique_key !~ '^TA\d{4}\.T\d{4}(\.\d{3})?$');

DELETE FROM mitre_technique
WHERE technique_key !~ '^TA\d{4}\.T\d{4}(\.\d{3})?$';

ALTER TABLE training_definition
    ADD COLUMN version int8 NOT NULL DEFAULT 0;

ALTER TABLE training_definition
    DROP COLUMN beta_testing_group_id;

DROP TABLE beta_testing_group_user_ref;

DROP TABLE beta_testing_group;

ALTER TABLE attachment DROP COLUMN creation_time;

CREATE EXTENSION IF NOT EXISTS unaccent WITH SCHEMA public;
CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;

CREATE FUNCTION search_fold(text) RETURNS text
    LANGUAGE sql IMMUTABLE PARALLEL SAFE STRICT
AS $$ SELECT lower(public.unaccent('public.unaccent', $1)) $$;

CREATE INDEX training_definition_title_search_fold_idx
    ON training_definition USING gin (search_fold(title) gin_trgm_ops);
CREATE INDEX training_definition_description_search_fold_idx
    ON training_definition USING gin (search_fold(description) gin_trgm_ops);
CREATE INDEX training_definition_last_edited_by_search_fold_idx
    ON training_definition USING gin (search_fold(last_edited_by) gin_trgm_ops);

CREATE INDEX training_instance_title_search_fold_idx
    ON training_instance USING gin (search_fold(title) gin_trgm_ops);
CREATE INDEX training_instance_access_token_search_fold_idx
    ON training_instance USING gin (search_fold(access_token) gin_trgm_ops);
CREATE INDEX training_instance_last_edited_by_search_fold_idx
    ON training_instance USING gin (search_fold(last_edited_by) gin_trgm_ops);

CREATE INDEX abstract_detection_event_level_title_search_fold_idx
    ON abstract_detection_event USING gin (search_fold(level_title) gin_trgm_ops);
CREATE INDEX abstract_detection_event_participants_search_fold_idx
    ON abstract_detection_event USING gin (search_fold(participants) gin_trgm_ops);

-- Existing instances keep the sandboxes their organizers allocated; new ones default to not managed.
ALTER TABLE training_instance ADD COLUMN managed boolean NOT NULL DEFAULT TRUE;
ALTER TABLE training_instance ALTER COLUMN managed SET DEFAULT FALSE;
