-- Level content attachments move from per-level rows to a global store of files keyed by the
-- SHA-256 of their bytes, linked to the definitions that use them.
DROP TABLE attachment;
ALTER TABLE training_level DROP COLUMN attachments;

CREATE TABLE attachment_file (
    hash       varchar(64)  NOT NULL,
    media_type varchar(255) NOT NULL,
    size_bytes int8         NOT NULL,
    content    bytea        NOT NULL,
    duration_seconds float8,
    width      int4,
    height     int4,
    poster     bytea,
    poster_media_type varchar(255),
    PRIMARY KEY (hash),
    CHECK (hash ~ '^[0-9a-f]{64}$'),
    CHECK ((poster IS NULL) = (poster_media_type IS NULL))
);

CREATE TABLE definition_attachment (
    training_definition_id int8         NOT NULL,
    file_hash              varchar(64)  NOT NULL,
    file_name              varchar(255) NOT NULL,
    PRIMARY KEY (training_definition_id, file_hash),
    FOREIGN KEY (training_definition_id) REFERENCES training_definition ON DELETE CASCADE,
    FOREIGN KEY (file_hash) REFERENCES attachment_file
);

CREATE INDEX definition_attachment_file_hash_idx ON definition_attachment (file_hash);

-- Question text holds markdown that embeds attachment references and has no length cap.
ALTER TABLE question ALTER COLUMN text TYPE text;
