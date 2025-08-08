-- V3__posts_add_subreddit_fk.sql

-- 0) UUID helper (una din ele e ok)
CREATE EXTENSION IF NOT EXISTS pgcrypto;
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1) Adaugă coloana FK (temporar NULL ca să o putem popula)
ALTER TABLE posts
    ADD COLUMN IF NOT EXISTS subreddit_id UUID;

-- 2) Dacă încă există vechiul posts.subreddit, migrează datele
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'posts' AND column_name = 'subreddit'
    ) THEN
        -- 2.1) Inserează subreddits distincte după numele vechi
        INSERT INTO subreddits (id, name, display_name, description, icon_url)
SELECT gen_random_uuid(), p.subreddit, p.subreddit, NULL, NULL
FROM posts p
WHERE p.subreddit IS NOT NULL
GROUP BY p.subreddit
    ON CONFLICT (name) DO NOTHING;

-- 2.2) Populează FK în posts
UPDATE posts p
SET subreddit_id = s.id
    FROM subreddits s
WHERE p.subreddit = s.name
  AND (p.subreddit_id IS DISTINCT FROM s.id OR p.subreddit_id IS NULL);

-- 2.3) După populare, dacă nu mai sunt NULL-uri, fă NOT NULL
IF NOT EXISTS (SELECT 1 FROM posts WHERE subreddit_id IS NULL) THEN
ALTER TABLE posts
    ALTER COLUMN subreddit_id SET NOT NULL;
END IF;

        -- 2.4) Șterge coloana veche
ALTER TABLE posts
DROP COLUMN subreddit;
ELSE
        -- Nu mai există coloana veche -> lăsăm NOT NULL pentru mai târziu
        RAISE NOTICE 'Coloana posts.subreddit nu există; asigurați popularea posts.subreddit_id la seed.';
END IF;
END $$;

-- 3) Adaugă FK (dacă lipsește)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_posts_subreddit'
          AND conrelid = 'posts'::regclass
    ) THEN
ALTER TABLE posts
    ADD CONSTRAINT fk_posts_subreddit
        FOREIGN KEY (subreddit_id) REFERENCES subreddits(id);
END IF;
END $$;

-- 4) Index pe FK
CREATE INDEX IF NOT EXISTS idx_posts_subreddit_id ON posts(subreddit_id);
