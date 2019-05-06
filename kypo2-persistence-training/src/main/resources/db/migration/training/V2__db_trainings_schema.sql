
    create table abstract_level (
       id  bigserial not null,
        estimated_duration int4,
        max_score int4 not null,
        order_in_training_definition int4 not null,
        title varchar(255) not null,
        snapshot_hook_id int8,
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
        solution varchar(255) not null,
        solution_penalized boolean not null,
        id int8 not null,
        primary key (id)
    );

    create table hint (
       id  bigserial not null,
        content varchar(255) not null,
        hint_penalty int4 not null,
        title varchar(255) not null,
        game_level_id int8,
        primary key (id)
    );

    create table info_level (
       content text not null,
        id int8 not null,
        primary key (id)
    );

    create table sandbox_instance_ref (
       id  bigserial not null,
        sandbox_instance_ref int8,
        training_instance_id int8 not null,
        primary key (id)
    );

    create table snapshot_hook (
       id  bigserial not null,
        snapshot text not null,
        primary key (id)
    );

    create table training_definition (
       id  bigserial not null,
        description text,
        last_edited timestamp not null,
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
        current_score int4,
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
        primary key (id)
    );

    create table user_ref (
       id  bigserial not null,
        user_ref_full_name varchar(255),
        user_ref_login varchar(255) not null,
        primary key (id)
    );

    alter table access_token
       add constraint UK_qglhb4xi0iwstguebaliifr1n unique (access_token);

    alter table training_definition
       add constraint UK_8k8if9s1vogmedxasdadcr4tb unique (beta_testing_group_id);

    alter table training_instance
       add constraint UK_b81w12g91hiuhdvsmoanyel6m unique (access_token);

    alter table user_ref
       add constraint UK_iajf018nptidl085leng237xl unique (user_ref_login);

    alter table abstract_level
       add constraint FKi9sciy07av8pb1yv3fl4ycby0
       foreign key (snapshot_hook_id)
       references snapshot_hook;

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

    alter table sandbox_instance_ref
       add constraint FK2j5jmin6ht1fl42nyd5wiqsjd
       foreign key (training_instance_id)
       references training_instance;

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
       add constraint FK6yn4e9w78a454vegxipn3cmvf
       foreign key (sandbox_instance_ref_id)
       references sandbox_instance_ref;

    alter table training_run
       add constraint FK7vajehsxurugwfg363f4ppb0s
       foreign key (training_instance_id)
       references training_instance;
