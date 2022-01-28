create table access_level (
    cloud_content text not null,
    local_content text not null,
    passkey varchar(255) not null,
    id int8 not null,
    primary key (id),
    foreign key (id) references abstract_level
);