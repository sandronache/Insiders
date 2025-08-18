CREATE INDEX IF NOT EXISTS idx_comments_post_created_at
    ON comments (post_id, created_at DESC);
