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
    created_at      timestamp   default CURRENT_TIMESTAMP not null,
    updated_at      timestamp   default CURRENT_TIMESTAMP not null
);


create table comments
(
    id                uuid                    not null
        primary key,
    post_id           uuid
        references posts
            on delete cascade,
    parent_comment_id uuid
        references comments
            on delete cascade,
    content           text                    not null,
    username          varchar(255)            not null
        references users
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
    username   varchar(255) not null
        references users
            on delete cascade,
    is_upvote  boolean      not null,
    created_at timestamp default CURRENT_TIMESTAMP
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
