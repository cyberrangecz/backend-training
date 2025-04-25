CREATE TABLE team_sandbox_lock (
    team_id                        bigint REFERENCES team (id),
    sandbox_instance_id            varchar(36),
    sandbox_instance_allocation_id int8
);