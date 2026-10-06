ALTER TABLE abstract_detection_event
    ALTER COLUMN training_run_id TYPE int8;

ALTER TABLE detection_event_participant
    ALTER COLUMN user_id TYPE int8 USING user_id::int8;

ALTER TABLE forbidden_command
    ALTER COLUMN command_type TYPE int2 USING command_type::int2;

ALTER TABLE detected_forbidden_command
    ALTER COLUMN command_type TYPE int2 USING command_type::int2;
