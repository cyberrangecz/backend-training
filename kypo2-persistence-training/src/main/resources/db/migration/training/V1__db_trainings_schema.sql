create table abstract_level (
   id  bigserial not null,
    estimated_duration int8,
    max_score int4 not null,
    order_in_training_definition int4 not null,
    title varchar(255) not null,
    training_definition_id int8,
    primary key (id)
);

create table access_token (
   id  bigserial not null,
    access_token varchar(255) not null,
    primary key (id)
);

create table assessment_level (
   assessment_type varchar(128) not null,
    instructions varchar(255) not null,
    questions text not null,
    id int8 not null,
    primary key (id)
);

create table beta_testing_group (
   id  bigserial not null,
    primary key (id)
);

create table beta_testing_group_user_ref (
   beta_testing_group_id int8 not null,
    user_ref_id int8 not null,
    primary key (beta_testing_group_id, user_ref_id)
);

create table game_level (
   attachments bytea,
    content text not null,
    flag varchar(255) not null,
    incorrect_flag_limit int4,
    solution text not null,
    solution_penalized boolean not null,
    id int8 not null,
    primary key (id)
);

create table hint (
    id  bigserial not null,
    content text not null,
    hint_penalty int4 not null,
    title varchar(255) not null,
    game_level_id int8,
    order_in_level int4 not null,
    primary key (id)
);

create table info_level (
   content text not null,
    id int8 not null,
    primary key (id)
);

create table training_definition (
   id  bigserial not null,
    description text,
    last_edited timestamp not null,
    estimated_duration int8,
    outcomes bytea,
    prerequisities bytea,
    sandbox_definition_ref_id int8 not null,
    show_stepper_bar boolean not null,
    state varchar(128) not null,
    title varchar(255) not null,
    beta_testing_group_id int8,
    primary key (id)
);

create table training_definition_user_ref (
   training_definition_id int8 not null,
    user_ref_id int8 not null,
    primary key (training_definition_id, user_ref_id)
);

create table training_instance (
   id  bigserial not null,
    access_token varchar(255) not null,
    end_time timestamp not null,
    pool_id int8,
    pool_size int4 not null,
    start_time timestamp not null,
    title varchar(255) not null,
    training_definition_id int8,
    primary key (id)
);

create table training_instance_user_ref (
   training_instance_id int8 not null,
    user_ref_id int8 not null,
    primary key (training_instance_id, user_ref_id)
);

create table training_run (
   id  bigserial not null,
    assessment_responses text,
    max_level_score int4,
    end_time timestamp not null,
    event_log_reference varchar(255),
    incorrect_flag_count int4 not null,
    level_answered boolean,
    solution_taken boolean not null,
    start_time timestamp not null,
    state varchar(128) not null,
    total_score int4,
    current_level_id int8 not null,
    user_ref_id int8 not null,
    sandbox_instance_ref_id int8 null,
    training_instance_id int8 not null,
    previous_sandbox_instance_ref_id int8 null,
    current_penalty int4,
    primary key (id)
);



create table hint_info(
  training_run_id bigserial not null,
  game_level_id bigserial not null,
  hint_id bigserial not null,
  hint_title varchar(128) not null,
  hint_content varchar(4096) not null,
  order_in_level int4 not null
);



create table user_ref (
   id  bigserial not null,
    user_ref_id int8 not null,
    primary key (id)
);

create table attachment (
    id bigserial not null,
    content varchar(255) not null ,
    creation_time timestamp not null,
    game_level_id int8,
    primary key (id)

);


alter table access_token
   add constraint UK_qglhb4xi0iwstguebaliifr1n unique (access_token);

alter table training_definition
   add constraint UK_8k8if9s1vogmedxasdadcr4tb unique (beta_testing_group_id);

alter table training_instance
   add constraint UK_b81w12g91hiuhdvsmoanyel6m unique (access_token);

alter table hint_info
   add constraint FKi9smgl25av8pb1yv3fl4ycby0
   foreign key (training_run_id)
   references training_run;

alter table abstract_level
   add constraint FK24361n3estpsxei7bx7sfvcxs
   foreign key (training_definition_id)
   references training_definition;

alter table assessment_level
   add constraint FK7jxec7ef838ovnrnfw73kh95
   foreign key (id)
   references abstract_level;

alter table beta_testing_group_user_ref
   add constraint FK4ph4xvdeggto33vg4g6cs18s1
   foreign key (user_ref_id)
   references user_ref;

alter table beta_testing_group_user_ref
   add constraint FKf01kbc9ae5599gnci7d0j2ldx
   foreign key (beta_testing_group_id)
   references beta_testing_group;

alter table game_level
   add constraint FKrg7pvp6aqm4gxshunqq77noma
   foreign key (id)
   references abstract_level;

alter table hint
   add constraint FKikeediy8uqdf22egpfmdaboor
   foreign key (game_level_id)
   references game_level;

alter table info_level
   add constraint FKa9ssogmfce6duhtlm8chrqcc4
   foreign key (id)
   references abstract_level;

alter table training_definition
   add constraint FKdps9cuy3u6c6v1n8igr9hnu1r
   foreign key (beta_testing_group_id)
   references beta_testing_group;

alter table training_definition_user_ref
   add constraint FKq5ejeyb8ced1s2t9lv4ld1uyl
   foreign key (user_ref_id)
   references user_ref;

alter table training_definition_user_ref
   add constraint FK99adq71p6nym0emx1xvr4qk4
   foreign key (training_definition_id)
   references training_definition;

alter table training_instance
   add constraint FK28s41pqjyqwrni7thb54tidru
   foreign key (training_definition_id)
   references training_definition;

alter table training_instance_user_ref
   add constraint FK53k0sdbkfgu7ddn902b3x0fsy
   foreign key (user_ref_id)
   references user_ref;

alter table training_instance_user_ref
   add constraint FKj92s3xyn59494b3kwxbmbs9ct
   foreign key (training_instance_id)
   references training_instance;

alter table training_run
   add constraint FKddva9h2olm0h0aj9veb6jfe9r
   foreign key (current_level_id)
   references abstract_level;

alter table training_run
   add constraint FKmfyx8wi2fu400w1h6gikyp9cy
   foreign key (user_ref_id)
   references user_ref;

alter table training_run
   add constraint FK7vajehsxurugwfg363f4ppb0s
   foreign key (training_instance_id)
   references training_instance;

   alter table attachment
   add constraint FKikeediy8uqdf22egpfmdaaar
   foreign key (game_level_id)
   references game_level;
