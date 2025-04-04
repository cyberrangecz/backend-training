ALTER TABLE training_definition
    ADD COLUMN type varchar(16) NOT NULL DEFAULT 'LINEAR';

ALTER TABLE training_instance
    ADD COLUMN type   varchar(16) NOT NULL DEFAULT 'LINEAR',
    ADD max_team_size smallint    NOT NULL DEFAULT 12;

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

CREATE TABLE jeopardy_sublevel (
    id          bigserial PRIMARY KEY,
    description varchar(100),
    category_id bigint REFERENCES jeopardy_category (id),
    level_type  varchar(16),
    FOREIGN KEY (id) REFERENCES training_level
);

CREATE TABLE team (
    id                   bigserial PRIMARY KEY,
    name                 varchar(64) NOT NULL,
    locked               boolean     NOT NULL,
    training_instance_id bigint REFERENCES training_instance (id),
    UNIQUE (name, training_instance_id)
);

CREATE TABLE team_user (
    team_id     bigint REFERENCES team (id),
    user_ref_id bigint REFERENCES user_ref (id),
    PRIMARY KEY (team_id, user_ref_id)
);

CREATE TABLE training_instance_waiting_users (
    training_instance_id bigint REFERENCES training_instance (id),
    user_ref_id          bigint REFERENCES user_ref (id),
    PRIMARY KEY (training_instance_id, user_ref_id)
);
