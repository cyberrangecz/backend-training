create table mitre_technique (
    id  bigserial not null,
    technique_key varchar(64) not null unique,
    primary key (id)
);

create table training_level_mitre_technique (
    training_level_id int8 not null,
    mitre_technique_id int8 not null,
    primary key (training_level_id, mitre_technique_id),
    foreign key (training_level_id) references training_level,
    foreign key (mitre_technique_id) references mitre_technique
);

create table expected_commands (
    training_level_id int8 not null,
    command varchar(128) not null,
    primary key (training_level_id, command),
    foreign key (training_level_id) references training_level
);

create index training_level_expected_commands_index
    on expected_commands (training_level_id);
