create table users
(
    id              uuid         not null
        primary key,
    username        varchar(255) not null
        unique,
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
    user_id         uuid                    not null
        references users(id)
            on delete cascade,
    subreddit       varchar(100)            not null,
    created_at      timestamp   default CURRENT_TIMESTAMP not null,
    updated_at      timestamp   default CURRENT_TIMESTAMP not null
);


create table comments
(
    id                uuid                    not null
        primary key,
    post_id           uuid                    not null
        references posts
            on delete cascade,
    parent_comment_id uuid
        references comments
            on delete cascade,
    content           text                    not null,
    user_id         uuid                    not null
        references users(id)
            on delete cascade,
    is_deleted        boolean     default false not null,
    created_at        timestamp   default CURRENT_TIMESTAMP not null,
    updated_at        timestamp   default CURRENT_TIMESTAMP not null
);

create table votes
(
    id         uuid         not null
        primary key,
    post_id    uuid
        references posts
            on delete cascade,
    comment_id uuid
        references comments
            on delete cascade,
    user_id         uuid                    not null
        references users(id)
            on delete cascade,
    is_upvote  boolean      not null,
    created_at timestamp default CURRENT_TIMESTAMP
);

create index idx_votes_user_id on votes(user_id);
create index idx_votes_post_id on votes(post_id);
create index idx_votes_comment_id on votes(comment_id);

create index idx_posts_user_id on posts(user_id);

create index idx_comments_user_id on comments(user_id);
create index idx_comments_post_id on comments(post_id);
create index idx_comments_parent_comment_id on comments(parent_comment_id);