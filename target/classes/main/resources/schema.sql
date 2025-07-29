create table users
(
    username        varchar(255) not null
        primary key,
    email           varchar(255) not null
        unique,
    hashed_password integer      not null,
    created_at      timestamp default CURRENT_TIMESTAMP
);

create table posts
(
    id              uuid                    not null
        primary key,
    title           varchar(300)            not null,
    content         text,
    username        varchar(255)            not null
        references users
            on delete cascade,
    subreddit       varchar(100)            not null,
    upvotes         integer     default 1   not null,
    downvotes       integer     default 0   not null,
    comment_count   integer     default 0   not null,
    id_next_comment integer     default 0   not null,
    created_at      timestamp   default CURRENT_TIMESTAMP not null,
    updated_at      timestamp   default CURRENT_TIMESTAMP not null
);

create index idx_posts_username
    on posts (username);

create index idx_posts_created_at
    on posts (created_at desc);

create index idx_posts_subreddit
    on posts (subreddit);

create index idx_posts_score
    on posts ((upvotes - downvotes) desc);

create index idx_posts_subreddit_created_at
    on posts (subreddit, created_at desc);

create table comments
(
    id                serial
        primary key,
    post_id           uuid                    not null
        references posts
            on delete cascade,
    parent_comment_id integer
        references comments
            on delete cascade,
    content           text                    not null,
    username          varchar(255)            not null
        references users
            on delete cascade,
    upvotes           integer     default 0   not null,
    downvotes         integer     default 0   not null,
    id_next_reply     integer     default 0   not null,
    is_deleted        boolean     default false not null,
    created_at        timestamp   default CURRENT_TIMESTAMP not null,
    updated_at        timestamp   default CURRENT_TIMESTAMP not null
);

create index idx_comments_post_id
    on comments (post_id);

create index idx_comments_parent_id
    on comments (parent_comment_id);

create index idx_comments_username
    on comments (username);

create index idx_comments_score
    on comments ((upvotes - downvotes) desc);

create table post_votes
(
    post_id    uuid         not null
        references posts
            on delete cascade,
    username   varchar(255) not null
        references users
            on delete cascade,
    is_upvote  boolean      not null,
    created_at timestamp default CURRENT_TIMESTAMP,
    primary key (post_id, username)
);

create index idx_post_votes_post_id
    on post_votes (post_id);

create table comment_votes
(
    comment_id integer      not null
        references comments
            on delete cascade,
    username   varchar(255) not null
        references users
            on delete cascade,
    is_upvote  boolean      not null,
    created_at timestamp default CURRENT_TIMESTAMP,
    primary key (comment_id, username)
);

create index idx_comment_votes_comment_id
    on comment_votes (comment_id);

create table post_emoji_flags
(
    post_id  uuid                  not null
        primary key
        references posts
            on delete cascade,
    is_emoji boolean default false not null
);

create table comment_emoji_flags
(
    comment_id integer               not null
        primary key
        references comments
            on delete cascade,
    is_emoji   boolean default false not null
);

create or replace function update_updated_at_column()
returns trigger as $$
begin
    new.updated_at = CURRENT_TIMESTAMP;
    return new;
end;
$$ language plpgsql;

create trigger update_posts_updated_at
    before update on posts
    for each row
    execute function update_updated_at_column();

create trigger update_comments_updated_at
    before update on comments
    for each row
    execute function update_updated_at_column();

create view posts_with_stats as
select
    p.id,
    p.title,
    p.content,
    p.username,
    p.subreddit,
    p.upvotes,
    p.downvotes,
    (p.upvotes - p.downvotes) as score,
    p.comment_count,
    p.created_at,
    p.updated_at,
    coalesce(pef.is_emoji, false) as is_emoji
from posts p
left join post_emoji_flags pef on p.id = pef.post_id;

create view comments_with_stats as
select
    c.id,
    c.post_id,
    c.parent_comment_id,
    c.content,
    c.username,
    c.upvotes,
    c.downvotes,
    (c.upvotes - c.downvotes) as score,
    c.is_deleted,
    c.created_at,
    c.updated_at,
    coalesce(cef.is_emoji, false) as is_emoji
from comments c
left join comment_emoji_flags cef on c.id = cef.comment_id;
