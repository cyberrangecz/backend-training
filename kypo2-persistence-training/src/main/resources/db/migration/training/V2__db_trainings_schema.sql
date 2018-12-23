CREATE TABLE abstract_level (
   id  bigserial NOT NULL,
    max_score int4 NOT NULL,
    next_level int8,
    title varchar(255) NOT NULL,
    post_hook_id int8,
    pre_hook_id int8,
    PRIMARY KEY (id)
);

CREATE TABLE assessment_level (
   assessment_type varchar(128) NOT NULL,
    instructions varchar(255) NOT NULL,
    questions text NOT NULL,
    id int8 NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE author_ref (
   id  bigserial NOT NULL,
    author_ref_login varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE game_level (
   attachments bytea,
    content varchar(255) NOT NULL,
    estimated_duration int4,
    flag varchar(255) NOT NULL,
    incorrect_flag_limit int4,
    solution varchar(255) NOT NULL,
    solution_penalized boolean NOT NULL,
    id int8 NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE hint (
   id  bigserial NOT NULL,
    content varchar(255) NOT NULL,
    hint_penalty int4 NOT NULL,
    title varchar(255) NOT NULL,
    game_level_id int8,
    PRIMARY KEY (id)
);

CREATE TABLE info_level (
   content text NOT NULL,
    id int8 NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE participant_ref (
   id  bigserial NOT NULL,
    participant_ref_login varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE password (
   id  bigserial NOT NULL,
    password varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE post_hook (
   id  bigserial NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE pre_hook (
   id  bigserial NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE sandbox_definition_ref (
   id  bigserial NOT NULL,
    sandbox_definition_ref int8,
    PRIMARY KEY (id)
);

CREATE TABLE sandbox_instance_ref (
   id  bigserial NOT NULL,
    sandbox_instance_ref int8,
    training_instance_id int8 NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE training_definition (
   id  bigserial NOT NULL,
    description varchar(255),
    outcomes bytea,
    prerequisities bytea,
    show_stepper_bar boolean NOT NULL,
    starting_level int8,
    state varchar(128) NOT NULL,
    title varchar(255) NOT NULL,
    sand_box_definition_ref_id int8,
    PRIMARY KEY (id)
);

CREATE TABLE training_definition_author_ref (
   training_definition_id int8 NOT NULL,
    author_ref_id int8 NOT NULL,
    PRIMARY KEY (training_definition_id, author_ref_id)
);

CREATE TABLE training_instance (
   id  bigserial NOT NULL,
    end_time timestamp NOT NULL,
    password varchar(255),
    pool_size int4 NOT NULL,
    start_time timestamp NOT NULL,
    title varchar(255) NOT NULL,
    training_definition_id int8 NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE training_instance_organizers (
   training_instance_id int8 NOT NULL,
    organizers_id int8 NOT NULL,
    PRIMARY KEY (training_instance_id, organizers_id)
);

CREATE TABLE training_instance_sandbox_instance_ref (
   training_instance_id int8 NOT NULL,
    sandbox_instance_ref_id int8 NOT NULL,
    PRIMARY KEY (training_instance_id, sandbox_instance_ref_id)
);

CREATE TABLE training_run (
   id  bigserial NOT NULL,
    assessment_responses text,
    current_score int4,
    end_time timestamp NOT NULL,
    event_log_reference varchar(255),
    incorrect_flag_count int4 NOT NULL,
    level_answered boolean,
    solution_taken boolean NOT NULL,
    start_time timestamp NOT NULL,
    state varchar(128) NOT NULL,
    total_score int4,
    current_level_id int8 NOT NULL,
    participant_ref_id int8 NOT NULL,
    sandbox_instance_ref_id int8 NOT NULL,
    training_instance_id int8 NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE user_ref (
   id  bigserial NOT NULL,
    user_ref_login varchar(255),
    PRIMARY KEY (id)
);

ALTER TABLE training_instance_sandbox_instance_ref
   ADD CONSTRAINT UK_qxd0j7mfffie0m8rfsjpqrlrv UNIQUE (sandbox_instance_ref_id);

ALTER TABLE training_run
   ADD CONSTRAINT UK_8g0gumb9rcvd0fscfxv1wb24c UNIQUE (sandbox_instance_ref_id);

ALTER TABLE abstract_level
   ADD CONSTRAINT FKfur32sh4k57g53x45w3d9mrv6
   FOREIGN KEY (post_hook_id)
   REFERENCES post_hook;

ALTER TABLE abstract_level
   ADD CONSTRAINT FKh97onob6w74379lvjq8jjiy1b
   FOREIGN KEY (pre_hook_id)
   REFERENCES pre_hook;

ALTER TABLE assessment_level
   ADD CONSTRAINT FK7jxec7ef838ovnrnfw73kh95
   FOREIGN KEY (id)
   REFERENCES abstract_level;

ALTER TABLE game_level
   ADD CONSTRAINT FKrg7pvp6aqm4gxshunqq77noma
   FOREIGN KEY (id)
   REFERENCES abstract_level;

ALTER TABLE hint
   ADD CONSTRAINT FKikeediy8uqdf22egpfmdaboor
   FOREIGN KEY (game_level_id)
   REFERENCES game_level;

ALTER TABLE info_level
   ADD CONSTRAINT FKa9ssogmfce6duhtlm8chrqcc4
   FOREIGN KEY (id)
   REFERENCES abstract_level;

ALTER TABLE sandbox_instance_ref
   ADD CONSTRAINT FK2j5jmin6ht1fl42nyd5wiqsjd
   FOREIGN KEY (training_instance_id)
   REFERENCES training_instance;

ALTER TABLE training_definition
   ADD CONSTRAINT FKlpslmg909yvgsihw6ribpcjee
   FOREIGN KEY (sand_box_definition_ref_id)
   REFERENCES sandbox_definition_ref;

ALTER TABLE training_definition_author_ref
   ADD CONSTRAINT FK76ifve9d4sreenamcmrwsh9tm
   FOREIGN KEY (author_ref_id)
   REFERENCES author_ref;

ALTER TABLE training_definition_author_ref
   ADD CONSTRAINT FK83b30979dnp5kade6m4600h7n
   FOREIGN KEY (training_definition_id)
   REFERENCES training_definition;

ALTER TABLE training_instance
   ADD CONSTRAINT FK28s41pqjyqwrni7thb54tidru
   FOREIGN KEY (training_definition_id)
   REFERENCES training_definition;

ALTER TABLE training_instance_organizers
   ADD CONSTRAINT FKofnq5p3x5u0o0c15a1oj9ckpx
   FOREIGN KEY (organizers_id)
   REFERENCES user_ref;

ALTER TABLE training_instance_organizers
   ADD CONSTRAINT FKe4qmx0nnbqxvg66wwt0si91vr
   FOREIGN KEY (training_instance_id)
   REFERENCES training_instance;

ALTER TABLE training_instance_sandbox_instance_ref
   ADD CONSTRAINT FK93ieg5ncp2jgxdn4b6ufty1wg
   FOREIGN KEY (sandbox_instance_ref_id)
   REFERENCES sandbox_instance_ref;

ALTER TABLE training_instance_sandbox_instance_ref
   ADD CONSTRAINT FKayuh2k5x6e1dwssc29p3jl58y
   FOREIGN KEY (training_instance_id)
   REFERENCES training_instance;

ALTER TABLE training_run
   ADD CONSTRAINT FKddva9h2olm0h0aj9veb6jfe9r
   FOREIGN KEY (current_level_id)
   REFERENCES abstract_level;

ALTER TABLE training_run
   ADD CONSTRAINT FKoseiqv9maacgplxk1xbriigw0
   FOREIGN KEY (participant_ref_id)
   REFERENCES participant_ref;

ALTER TABLE training_run
   ADD CONSTRAINT FK6yn4e9w78a454vegxipn3cmvf
   FOREIGN KEY (sandbox_instance_ref_id)
   REFERENCES sandbox_instance_ref;

ALTER TABLE training_run
   ADD CONSTRAINT FK7vajehsxurugwfg363f4ppb0s
   FOREIGN KEY (training_instance_id)
   REFERENCES training_instance;
