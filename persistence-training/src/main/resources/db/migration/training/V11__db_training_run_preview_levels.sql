create table solution_info(
    training_run_id bigserial not null,
    training_level_id bigserial not null,
    solution_content text not null,
    foreign key (training_run_id) references training_run
);

alter table training_instance add column backward_mode boolean not null default (false);