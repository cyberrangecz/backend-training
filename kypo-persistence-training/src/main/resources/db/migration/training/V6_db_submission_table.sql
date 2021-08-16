create table submission (
    id bigserial not null,
    provided text not null,
    type varchar(255) not null,
    ip_address varchar(255) not null,
    primary key (id),
    foreign key (level_id) references abstract_level,
    foreign key (training_run_id) references training_run
);