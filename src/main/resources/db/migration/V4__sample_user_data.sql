CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO fileway."user" (user_id, user_email, user_password, user_role)
VALUES
    (DEFAULT, 'test_1@gmail.com',crypt('password123', gen_salt('bf')), 'ROLE_USER'),
    (DEFAULT, 'test_2@gmail.com',crypt('password123', gen_salt('bf')), 'ROLE_PREMIUM');

