ALTER TABLE detection_event_participant
ADD COLUMN cheating_detection_id int8 not null,
ADD COLUMN detection_event_id int8 not null;