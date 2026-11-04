-- New training instances use the sandboxes their organizers allocate unless set otherwise.
ALTER TABLE training_instance ALTER COLUMN managed SET DEFAULT TRUE;
