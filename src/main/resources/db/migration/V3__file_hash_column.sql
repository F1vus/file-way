DELETE FROM fileway.file;

ALTER TABLE fileway.file ADD COLUMN file_hash VARCHAR(256) NOT NULL;