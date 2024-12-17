create table access_level (
    cloud_content text not null,
    local_content text not null,
    passkey varchar(255) not null,
    id int8 not null,
    primary key (id),
    foreign key (id) references abstract_level
);

alter table training_instance add column local_environment boolean default (false);
alter table training_instance add column sandbox_definition_id int8 default (null);