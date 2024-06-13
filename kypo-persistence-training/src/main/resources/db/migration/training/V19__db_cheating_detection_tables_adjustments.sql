alter table forbidden_command drop constraint forbidden_command_cheating_detection_id_fkey;
alter table forbidden_command alter column cheating_detection_id drop not null;

alter table abstract_detection_event add column level_order int4;

alter table abstract_detection_event add column training_run_id int4;

alter table abstract_detection_event add column participants text not null;

alter table forbidden_commands_detection_event
    add column command_count int8 not null,
    alter column forbidden_commands drop not null;

create table detected_forbidden_command (
   id bigserial not null,
    command varchar(255) not null,
    command_type varchar(255) not null,
    detection_event_id int8 not null,
    primary key (id)
);

alter table detected_forbidden_command add column hostname varchar(255);
alter table detected_forbidden_command add column occurred_at timestamp;