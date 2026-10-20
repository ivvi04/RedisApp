-- liquibase formatted sql

-- changeset Lakeev-DA:baseline-1
CREATE TABLE IF NOT EXISTS redis_app.users
(
    id        bigserial primary key         not null,
    fio       text                          not null,
    birthday  date                          not null
);
-- rollback DROP TABLE IF EXISTS redis_app.users;
