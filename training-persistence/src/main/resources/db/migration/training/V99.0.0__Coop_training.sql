ALTER TABLE training_definition
    ADD COLUMN type varchar(16) NOT NULL DEFAULT 'LINEAR';

ALTER TABLE training_instance
    ADD COLUMN type varchar(16) NOT NULL DEFAULT 'LINEAR';

CREATE TABLE jeopardy_level (
    id bigserial PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES abstract_level
);

CREATE TABLE jeopardy_category (
    id       bigserial PRIMARY KEY,
    title    text    NOT NULL,
    color    integer NOT NULL,
    level_id bigserial REFERENCES jeopardy_level (id)
);

ALTER TABLE training_level
    ADD COLUMN description varchar(100),
    ADD COLUMN category_id bigint REFERENCES jeopardy_category (id),
    ADD COLUMN level_type  varchar(16);


CREATE TABLE team (
    id                   bigserial PRIMARY KEY,
    name                 varchar(64) NOT NULL,
    training_instance_id bigserial REFERENCES training_instance (id)
);

CREATE TABLE team_user (
    team_id     bigint REFERENCES team (id),
    user_ref_id bigint REFERENCES user_ref (id),
    PRIMARY KEY (team_id, user_ref_id)
);


CREATE TABLE coop_instance_queue (
    id                   serial PRIMARY KEY,
    training_instance_id bigint REFERENCES training_instance (id)
);

CREATE TABLE coop_instance_queue_waiting_users (
    coop_instance_queue_id bigint REFERENCES coop_instance_queue (id),
    user_ref_id            bigint REFERENCES user_ref (id),
    PRIMARY KEY (coop_instance_queue_id, user_ref_id)
);

CREATE TABLE coop_instance_queue_prepared_teams (
    coop_instance_queue_id bigint REFERENCES coop_instance_queue (id) ON DELETE CASCADE,
    team_id                bigint REFERENCES team (id) ON DELETE CASCADE,
    PRIMARY KEY (coop_instance_queue_id, team_id)
);


