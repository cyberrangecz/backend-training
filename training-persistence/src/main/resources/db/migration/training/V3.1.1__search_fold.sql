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
