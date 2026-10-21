-- liquibase formatted sql

-- changeset lakeev-da:baseline-1
CREATE TABLE IF NOT EXISTS redis_app.users (
    id          serial          not null primary key,
    email       text            not null unique,
    username    varchar(255)    not null,
    password    varchar(255)    not null,
    created_at  timestamp
);
-- rollback DROP TABLE IF EXISTS redis_app.users;

-- changeset lakeev-da:baseline-2
CREATE TABLE IF NOT EXISTS redis_app.chats (
    id              serial      not null primary key,
    name            text,
    type            varchar(50) not null,
    created_at      timestamp   not null,
    created_by_id   bigint      not null references redis_app.users(id)
);
-- rollback DROP TABLE IF EXISTS redis_app.chats;

-- changeset lakeev-da:baseline-3
CREATE TABLE IF NOT EXISTS redis_app.chat_users (
    chat_id bigint not null references redis_app.chats(id),
    user_id bigint not null references redis_app.users(id),
    constraint chat_users_key unique (chat_id, user_id)
);
-- rollback DROP TABLE IF EXISTS redis_app.chat_users;

-- changeset lakeev-da:baseline-4
CREATE TABLE IF NOT EXISTS redis_app.messages (
    id          serial      not null primary key,
    sender_id   bigint      not null references redis_app.users(id),
    chat_id     bigint      not null references redis_app.chats(id),
    content     text        not null,
    timestamp   timestamp   not null,
    status      varchar(50) not null
);
-- rollback DROP TABLE IF EXISTS redis_app.messages;