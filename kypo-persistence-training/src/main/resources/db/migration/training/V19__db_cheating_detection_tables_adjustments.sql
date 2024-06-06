ALTER TABLE forbidden_command DROP CONSTRAINT forbidden_command_cheating_detection_id_fkey;
ALTER TABLE forbidden_command ALTER COLUMN cheating_detection_id DROP NOT NULL;

alter table detected_forbidden_command add column hostname varchar(255);
alter table detected_forbidden_command add column occurred_at timestamp;

alter table abstract_detection_event add column level_order int4;

alter table abstract_detection_event add column training_run_id int4;
alter table forbidden_commands_detection_event drop column training_run_id;

ALTER TABLE abstract_detection_event
    ADD COLUMN participants text NOT NULL;

ALTER TABLE forbidden_commands_detection_event
    ADD COLUMN training_run_id int8 NOT NULL,
    ADD COLUMN command_count int8 NOT NULL,
    ALTER COLUMN forbidden_commands DROP NOT NULL;

CREATE TABLE detected_forbidden_command (
   id bigserial NOT NULL,
    command varchar(255) NOT NULL,
    command_type varchar(255) NOT NULL,
    detection_event_id int8 NOT NULL,
    PRIMARY KEY (id)
);