ALTER TABLE fileway.file DROP COLUMN file_link_id;

ALTER TABLE fileway.file_link ADD COLUMN file_id BIGINT NOT NULL;