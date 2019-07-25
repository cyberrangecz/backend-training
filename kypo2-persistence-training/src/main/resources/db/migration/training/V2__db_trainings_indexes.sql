CREATE INDEX abstract_level_order_in_training_definition_index
ON abstract_level (order_in_training_definition);

CREATE UNIQUE INDEX access_token_access_token_index
ON access_token (access_token);

CREATE UNIQUE INDEX training_instance_access_token_index
ON training_instance (access_token);

CREATE INDEX training_instance_start_time_and_end_time_index
ON training_instance (start_time, end_time DESC);

CREATE INDEX training_definition_state_index
ON training_definition (state);

CREATE INDEX training_run_start_time_and_end_time_index
ON training_run (start_time, end_time DESC);

CREATE INDEX user_ref_user_ref_login_index
ON user_ref (user_ref_login);
