DELETE FROM training_level_mitre_technique
WHERE mitre_technique_id IN (SELECT id
                             FROM mitre_technique
                             WHERE technique_key !~ '^TA\d{4}\.T\d{4}(\.\d{3})?$');

DELETE FROM mitre_technique
WHERE technique_key !~ '^TA\d{4}\.T\d{4}(\.\d{3})?$';

ALTER TABLE training_definition
    ADD COLUMN version int8 NOT NULL DEFAULT 0;

ALTER TABLE training_definition
    DROP COLUMN beta_testing_group_id;

DROP TABLE beta_testing_group_user_ref;

DROP TABLE beta_testing_group;

ALTER TABLE attachment DROP COLUMN creation_time;
