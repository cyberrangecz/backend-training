CREATE TABLE idm_group_ref (
   id             BIGSERIAL NOT NULL,
   group_id       int8 NOT NULL UNIQUE,
   PRIMARY KEY (id));

CREATE TABLE role (
   id          BIGSERIAL NOT NULL,
   role_type   varchar(255) NOT NULL UNIQUE,
   PRIMARY KEY (id));

CREATE TABLE idm_group_role (
   role_id        int8 NOT NULL,
   idm_group_ref_id   int8 NOT NULL,
   PRIMARY KEY (role_id, idm_group_ref_id));

ALTER TABLE idm_group_role ADD CONSTRAINT FKidm_group_284474 FOREIGN KEY (role_id) REFERENCES role (id);
ALTER TABLE idm_group_role ADD CONSTRAINT FKidm_group_389301 FOREIGN KEY (idm_group_ref_id) REFERENCES idm_group_ref (id);