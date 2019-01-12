CREATE TABLE abstract_level (
   id  bigserial NOT NULL,
    max_score int4 NOT NULL,
    next_level int8,
    title varchar(255) NOT NULL,
    post_hook_id int8,
    pre_hook_id int8,
    PRIMARY KEY (id)
);

CREATE TABLE access_token (
   id  bigserial NOT NULL,
    access_token varchar(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE assessment_level (
   assessment_type varchar(128) NOT NULL,
    instructions varchar(255) NOT NULL,
    questions text NOT NULL,
    id int8 NOT NULL,
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

CREATE TABLE post_hook (
   id  bigserial NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE pre_hook (
   id  bigserial NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE sandbox_instance_ref (
   id  bigserial NOT NULL,
    sandbox_instance_ref int8,
    training_instance_id int8 NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE td_view_group (
   id  bigserial NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE td_view_group_user_ref (
   td_view_group_id int8 NOT NULL,
    user_ref_id int8 NOT NULL,
    PRIMARY KEY (td_view_group_id, user_ref_id)
);

CREATE TABLE training_definition (
   id  bigserial NOT NULL,
    description varchar(255),
    outcomes bytea,
    prerequisities bytea,
    sandbox_definition_ref_id int8,
    show_stepper_bar boolean NOT NULL,
    starting_level int8,
    state varchar(128) NOT NULL,
    title varchar(255) NOT NULL,
    td_view_group_id int8,
    PRIMARY KEY (id)
);

CREATE TABLE training_definition_user_ref (
   training_definition_id int8 NOT NULL,
    user_ref_id int8 NOT NULL,
    PRIMARY KEY (training_definition_id, user_ref_id)
);

CREATE TABLE training_instance (
   id  bigserial NOT NULL,
    access_token varchar(255),
    end_time timestamp NOT NULL,
    pool_size int4 NOT NULL,
    start_time timestamp NOT NULL,
    title varchar(255) NOT NULL,
    training_definition_id int8,
    PRIMARY KEY (id)
);

CREATE TABLE training_instance_user_ref (
   training_instance_id int8 NOT NULL,
    user_ref_id int8 NOT NULL,
    PRIMARY KEY (training_instance_id, user_ref_id)
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
    user_ref_id int8 NOT NULL,
    sandbox_instance_ref_id int8 NOT NULL,
    training_instance_id int8 NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE user_ref (
   id  bigserial NOT NULL,
    user_ref_login varchar(255),
    PRIMARY KEY (id)
);

ALTER TABLE access_token
   ADD CONSTRAINT UK_qglhb4xi0iwstguebaliifr1n unique (access_token);

ALTER TABLE training_run
   ADD CONSTRAINT UK_8g0gumb9rcvd0fscfxv1wb24c unique (sandbox_instance_ref_id);

ALTER TABLE user_ref
   ADD CONSTRAINT UK_iajf018nptidl085leng237xl unique (user_ref_login);

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

ALTER TABLE td_view_group_user_ref
   ADD CONSTRAINT FKh2gwvi7oxr8uqcs9yf6352bo0
   FOREIGN KEY (user_ref_id)
   REFERENCES user_ref;

ALTER TABLE td_view_group_user_ref
   ADD CONSTRAINT FKk97tvstrwj10cnic68e9i5bjd
   FOREIGN KEY (td_view_group_id)
   REFERENCES td_view_group;

ALTER TABLE training_definition
   ADD CONSTRAINT FKb7vjqot5ntr08c948ttkg20f0
   FOREIGN KEY (td_view_group_id)
   REFERENCES td_view_group;

ALTER TABLE training_definition_user_ref
   ADD CONSTRAINT FKq5ejeyb8ced1s2t9lv4ld1uyl
   FOREIGN KEY (user_ref_id)
   REFERENCES user_ref;

ALTER TABLE training_definition_user_ref
   ADD CONSTRAINT FK99adq71p6nym0emx1xvr4qk4
   FOREIGN KEY (training_definition_id)
   REFERENCES training_definition;

ALTER TABLE training_instance
   ADD CONSTRAINT FK28s41pqjyqwrni7thb54tidru
   FOREIGN KEY (training_definition_id)
   REFERENCES training_definition;

ALTER TABLE training_instance_user_ref
   ADD CONSTRAINT FK53k0sdbkfgu7ddn902b3x0fsy
   FOREIGN KEY (user_ref_id)
   REFERENCES user_ref;

ALTER TABLE training_instance_user_ref
   ADD CONSTRAINT FKj92s3xyn59494b3kwxbmbs9ct
   FOREIGN KEY (training_instance_id)
   REFERENCES training_instance;

ALTER TABLE training_run
   ADD CONSTRAINT FKddva9h2olm0h0aj9veb6jfe9r
   FOREIGN KEY (current_level_id)
   REFERENCES abstract_level;

ALTER TABLE training_run
   ADD CONSTRAINT FKmfyx8wi2fu400w1h6gikyp9cy
   FOREIGN KEY (user_ref_id)
   REFERENCES user_ref;

ALTER TABLE training_run
   ADD CONSTRAINT FK6yn4e9w78a454vegxipn3cmvf
   FOREIGN KEY (sandbox_instance_ref_id)
   REFERENCES sandbox_instance_ref;

ALTER TABLE training_run
   ADD CONSTRAINT FK7vajehsxurugwfg363f4ppb0s
   FOREIGN KEY (training_instance_id)
   REFERENCES training_instance;
