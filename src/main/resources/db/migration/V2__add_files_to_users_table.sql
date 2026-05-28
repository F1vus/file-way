ALTER TABLE fileway.file ADD COLUMN user_id BIGINT;

ALTER TABLE fileway.file ADD CONSTRAINT fk_file_user_id FOREIGN KEY (user_id) REFERENCES fileway."user"(user_id) ON DELETE CASCADE;