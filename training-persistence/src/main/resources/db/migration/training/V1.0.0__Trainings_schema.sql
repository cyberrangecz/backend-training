CREATE TABLE user_ref (
    id          bigserial NOT NULL,
    user_ref_id int8      NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE beta_testing_group (
    id bigserial NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE beta_testing_group_user_ref (
    beta_testing_group_id int8 NOT NULL,
    user_ref_id           int8 NOT NULL,
    PRIMARY KEY (beta_testing_group_id, user_ref_id),
    FOREIGN KEY (beta_testing_group_id) REFERENCES beta_testing_group,
    FOREIGN KEY (user_ref_id) REFERENCES user_ref
);

CREATE TABLE training_definition (
    id                    bigserial    NOT NULL,
    description           text,
    last_edited           timestamp    NOT NULL,
    last_edited_by        varchar(127) NOT NULL DEFAULT '',
    estimated_duration    int8,
    outcomes              bytea,
    prerequisites         bytea,
    state                 varchar(128) NOT NULL,
    title                 varchar(255) NOT NULL,
    beta_testing_group_id int8 UNIQUE,
    created_at            timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (beta_testing_group_id) REFERENCES beta_testing_group
);

CREATE TABLE training_definition_user_ref (
    training_definition_id int8 NOT NULL,
    user_ref_id            int8 NOT NULL,
    PRIMARY KEY (training_definition_id, user_ref_id),
    FOREIGN KEY (training_definition_id) REFERENCES training_definition,
    FOREIGN KEY (user_ref_id) REFERENCES user_ref
);

CREATE TABLE training_instance (
    id                     bigserial    NOT NULL,
    access_token           varchar(255) NOT NULL UNIQUE,
    end_time               timestamp    NOT NULL,
    pool_id                int8,
    start_time             timestamp    NOT NULL,
    title                  varchar(255) NOT NULL,
    training_definition_id int8,
    last_edited            timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_edited_by         varchar(127) NOT NULL DEFAULT '',
    local_environment      boolean               DEFAULT (FALSE),
    sandbox_definition_id  int8                  DEFAULT (NULL),
    backward_mode          boolean      NOT NULL DEFAULT (FALSE),
    show_stepper_bar       boolean      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    FOREIGN KEY (training_definition_id) REFERENCES training_definition
);

CREATE TABLE training_instance_user_ref (
    training_instance_id int8 NOT NULL UNIQUE,
    user_ref_id          int8 NOT NULL,
    PRIMARY KEY (training_instance_id, user_ref_id),
    FOREIGN KEY (training_instance_id) REFERENCES training_instance,
    FOREIGN KEY (user_ref_id) REFERENCES user_ref
);

CREATE TABLE abstract_level (
    id                           bigserial    NOT NULL,
    estimated_duration           int8,
    max_score                    int4         NOT NULL,
    order_in_training_definition int4         NOT NULL,
    title                        varchar(255) NOT NULL,
    training_definition_id       int8,
    minimal_possible_solve_time  int8 DEFAULT (NULL),
    PRIMARY KEY (id),
    FOREIGN KEY (training_definition_id) REFERENCES training_definition
);

CREATE TABLE training_run (
    id                               bigserial    NOT NULL,
    assessment_responses             text,
    max_level_score                  int4,
    end_time                         timestamp    NOT NULL,
    event_log_reference              varchar(255),
    incorrect_answer_count           int4         NOT NULL,
    level_answered                   boolean,
    solution_taken                   boolean      NOT NULL,
    start_time                       timestamp    NOT NULL,
    state                            varchar(128) NOT NULL,
    current_level_id                 int8         NOT NULL,
    user_ref_id                      int8         NOT NULL,
    sandbox_instance_ref_id          varchar(36)  NULL,
    training_instance_id             int8         NOT NULL,
    previous_sandbox_instance_ref_id varchar(36)  NULL,
    current_penalty                  int4,
    total_training_score             int4                  DEFAULT (0),
    total_assessment_score           int4                  DEFAULT (0),
    has_detection_event              boolean      NOT NULL DEFAULT (FALSE),
    sandbox_instance_allocation_id   int8,
    PRIMARY KEY (id),
    FOREIGN KEY (current_level_id) REFERENCES abstract_level,
    FOREIGN KEY (user_ref_id) REFERENCES user_ref,
    FOREIGN KEY (training_instance_id) REFERENCES training_instance
);


CREATE TABLE info_level (
    content text NOT NULL,
    id      int8 NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES abstract_level
);


CREATE TABLE access_level (
    cloud_content text         NOT NULL,
    local_content text         NOT NULL,
    passkey       varchar(255) NOT NULL,
    id            int8         NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES abstract_level
);

CREATE TABLE assessment_level (
    assessment_type varchar(128)  NOT NULL,
    instructions    varchar(1023) NOT NULL,
    id              int8          NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES abstract_level
);

CREATE TABLE training_level (
    attachments            bytea,
    content                text    NOT NULL,
    answer                 varchar(255),
    incorrect_answer_limit int4,
    solution               text    NOT NULL,
    solution_penalized     boolean NOT NULL,
    id                     int8    NOT NULL,
    answer_variable_name   varchar(255),
    variant_answers        boolean          DEFAULT (FALSE),
    reference_solution     text             DEFAULT '[]',
    commands_required      boolean NOT NULL DEFAULT (TRUE),
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES abstract_level
);

CREATE TABLE hint (
    id                bigserial    NOT NULL,
    content           text         NOT NULL,
    hint_penalty      int4         NOT NULL,
    title             varchar(255) NOT NULL,
    training_level_id int8,
    order_in_level    int4         NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (training_level_id) REFERENCES training_level
);

CREATE TABLE hint_info (
    training_run_id   bigserial     NOT NULL,
    training_level_id bigserial     NOT NULL,
    hint_id           bigserial     NOT NULL,
    hint_title        varchar(128)  NOT NULL,
    hint_content      varchar(4096) NOT NULL,
    order_in_level    int4          NOT NULL,
    FOREIGN KEY (training_run_id) REFERENCES training_run
);


CREATE TABLE attachment (
    id                bigserial    NOT NULL,
    content           varchar(255) NOT NULL,
    creation_time     timestamp    NOT NULL,
    training_level_id int8,
    PRIMARY KEY (id),
    FOREIGN KEY (training_level_id) REFERENCES training_level
);

CREATE TABLE training_run_acquisition_lock (
    id                   bigserial NOT NULL,
    participant_ref_id   bigserial NOT NULL UNIQUE,
    training_instance_id bigserial NOT NULL,
    creation_time        timestamp NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (participant_ref_id, training_instance_id)
);

CREATE TABLE question (
    question_id         bigserial    NOT NULL,
    question_type       varchar(64)  NOT NULL,
    order_in_assessment int4         NOT NULL,
    points              int4         NOT NULL,
    penalty             int4         NOT NULL,
    text                varchar(255) NOT NULL,
    answer_required     boolean,
    assessment_level_id int8         NOT NULL,
    PRIMARY KEY (question_id),
    FOREIGN KEY (assessment_level_id) REFERENCES assessment_level
);

CREATE TABLE question_choice (
    question_choice_id bigserial NOT NULL,
    correct            boolean   NOT NULL,
    text               text      NOT NULL,
    order_in_question  int4      NOT NULL,
    question_id        int8      NOT NULL,
    PRIMARY KEY (question_choice_id),
    FOREIGN KEY (question_id) REFERENCES question
);

CREATE TABLE extend_matching_option (
    extend_matching_option_id bigserial NOT NULL,
    text                      text      NOT NULL,
    order_in_row              int4      NOT NULL,
    question_id               int8      NOT NULL,
    PRIMARY KEY (extend_matching_option_id),
    FOREIGN KEY (question_id) REFERENCES question
);

CREATE TABLE extended_matching_statement (
    extended_matching_statement_id bigserial NOT NULL,
    text                           text      NOT NULL,
    order_in_column                int4      NOT NULL,
    question_id                    int8      NOT NULL,
    extended_matching_option_id    int8,
    PRIMARY KEY (extended_matching_statement_id),
    FOREIGN KEY (question_id) REFERENCES question,
    FOREIGN KEY (extended_matching_option_id) REFERENCES extend_matching_option
);

CREATE TABLE question_answer (
    question_id     int8 NOT NULL,
    training_run_id int8 NOT NULL,
    PRIMARY KEY (question_id, training_run_id),
    FOREIGN KEY (question_id) REFERENCES question,
    FOREIGN KEY (training_run_id) REFERENCES training_run,
    UNIQUE (question_id, training_run_id)
);

CREATE TABLE question_answers (
    question_id     int8          NOT NULL,
    training_run_id int8          NOT NULL,
    answer          varchar(1023) NOT NULL,
    FOREIGN KEY (question_id, training_run_id) REFERENCES question_answer
);

CREATE TABLE submission (
    id              bigserial    NOT NULL,
    provided        text         NOT NULL,
    type            varchar(255) NOT NULL,
    level_id        int8         NOT NULL,
    training_run_id int8         NOT NULL,
    date            timestamp    NOT NULL,
    ip_address      varchar(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (level_id) REFERENCES abstract_level,
    FOREIGN KEY (training_run_id) REFERENCES training_run
);

CREATE TABLE mitre_technique (
    id            bigserial   NOT NULL,
    technique_key varchar(64) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE training_level_mitre_technique (
    training_level_id  int8 NOT NULL,
    mitre_technique_id int8 NOT NULL,
    PRIMARY KEY (training_level_id, mitre_technique_id),
    FOREIGN KEY (training_level_id) REFERENCES training_level,
    FOREIGN KEY (mitre_technique_id) REFERENCES mitre_technique
);

CREATE TABLE expected_commands (
    training_level_id int8         NOT NULL,
    command           varchar(128) NOT NULL,
    PRIMARY KEY (training_level_id, command),
    FOREIGN KEY (training_level_id) REFERENCES training_level
);

CREATE TABLE solution_info (
    training_run_id   bigserial NOT NULL,
    training_level_id bigserial NOT NULL,
    solution_content  text      NOT NULL,
    FOREIGN KEY (training_run_id) REFERENCES training_run
);

CREATE INDEX abstract_level_order_in_training_definition_index
    ON abstract_level (order_in_training_definition);

CREATE INDEX training_instance_start_time_and_end_time_index
    ON training_instance (start_time, end_time DESC);

CREATE INDEX training_definition_state_index
    ON training_definition (state);

CREATE INDEX training_run_start_time_and_end_time_index
    ON training_run (start_time, end_time DESC);

CREATE INDEX training_level_expected_commands_index
    ON expected_commands (training_level_id);

/*--------------------- Cheating detection ---------------------*/

CREATE TABLE abstract_detection_event (
    id                    bigserial    NOT NULL,
    training_instance_id  int8         NOT NULL,
    cheating_detection_id int8         NOT NULL,
    level_id              int8         NOT NULL,
    level_title           varchar(255) NOT NULL,
    detected_at           timestamp    NOT NULL,
    participant_count     int8         NOT NULL,
    detection_event_type  text         NOT NULL,
    participants          text         NOT NULL,
    level_order           int4,
    training_run_id       int4,
    PRIMARY KEY (id)
);

CREATE TABLE answer_similarity_detection_event (
    id           bigserial    NOT NULL,
    answer       varchar(255) NOT NULL,
    answer_owner varchar(255) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES abstract_detection_event
);

CREATE TABLE location_similarity_detection_event (
    id                bigserial    NOT NULL,
    ip_address        varchar(255) NOT NULL,
    dns               varchar(255) NOT NULL,
    is_address_deploy boolean      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES abstract_detection_event
);

CREATE TABLE time_proximity_detection_event (
    id        bigserial NOT NULL,
    threshold int8      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES abstract_detection_event
);

CREATE TABLE minimal_solve_time_detection_event (
    id                 bigserial NOT NULL,
    minimal_solve_time int8      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES abstract_detection_event
);

CREATE TABLE no_commands_detection_event (
    id bigserial NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES abstract_detection_event
);

CREATE TABLE forbidden_commands_detection_event (
    id                 bigserial NOT NULL,
    forbidden_commands text,
    command_count      int8      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES abstract_detection_event
);

CREATE TABLE detected_forbidden_command (
    id                 bigserial    NOT NULL,
    command            varchar(255) NOT NULL,
    command_type       varchar(255) NOT NULL,
    detection_event_id int8         NOT NULL,
    hostname           varchar(255),
    occurred_at        timestamp,
    PRIMARY KEY (id)
);

CREATE TABLE cheating_detection (
    id                        bigserial    NOT NULL,
    training_instance_id      int8         NOT NULL,
    executed_by               varchar(255) NOT NULL,
    execute_time              timestamp    NOT NULL,
    proximity_threshold       int8,
    results                   int8         NOT NULL,
    current_state             text         NOT NULL,
    answer_similarity_state   text         NOT NULL,
    location_similarity_state text         NOT NULL,
    time_proximity_state      text         NOT NULL,
    minimal_solve_time_state  text         NOT NULL,
    forbidden_commands_state  text         NOT NULL,
    no_commands_state         text         NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE forbidden_command (
    id                    bigserial    NOT NULL,
    command               varchar(255) NOT NULL,
    command_type          varchar(255) NOT NULL,
    cheating_detection_id int8,
    PRIMARY KEY (id)
);

CREATE TABLE detection_event_participant (
    id                    bigserial    NOT NULL,
    user_id               varchar(255) NOT NULL,
    ip_address            varchar(255) NOT NULL,
    occurred_at           timestamp,
    participant_name      varchar(255),
    solved_in_time        int8,
    detection_event_id    int8         NOT NULL,
    cheating_detection_id int8         NOT NULL,
    PRIMARY KEY (id)
);

CREATE SEQUENCE question_choice_seq AS bigint INCREMENT 50 MINVALUE 1;
CREATE SEQUENCE question_seq AS bigint INCREMENT 50 MINVALUE 1;
CREATE SEQUENCE extended_matching_option_seq AS bigint INCREMENT 50 MINVALUE 1;
CREATE SEQUENCE extended_matching_statement_seq AS bigint INCREMENT 50 MINVALUE 1;