-- Existing instances keep the sandboxes their organizers allocated; new ones default to not managed.
ALTER TABLE training_instance ADD COLUMN managed boolean NOT NULL DEFAULT TRUE;
ALTER TABLE training_instance ALTER COLUMN managed SET DEFAULT FALSE;
