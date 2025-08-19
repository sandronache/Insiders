-- posts
ALTER TABLE posts
    ALTER COLUMN updated_at DROP DEFAULT,
ALTER COLUMN updated_at DROP NOT NULL;

-- comments
ALTER TABLE comments
    ALTER COLUMN updated_at DROP DEFAULT,
ALTER COLUMN updated_at DROP NOT NULL;