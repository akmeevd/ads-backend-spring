-- liquibase formatted sql

-- changeSet 11th:1
create table if not exists images
(
    id              serial primary key not null,
    image_type      varchar(10),
    image_dir       varchar(50),
    file_type       varchar(30),
    file_name       varchar(100),
    file_extension  varchar(10),
    file_size       bigint
);

create table if not exists users
(
    id         serial primary key not null,
    password   text               not null,
    first_name varchar(70),
    last_name  varchar(70),
    username   varchar(50) unique,
    phone      varchar(16),
    role       varchar(10),
    enabled   bool,
    image_id  integer references images(id)
);

create table if not exists adverts
(
    id          serial primary key not null,
    title       varchar(100)       not null,
    description text,
    price       integer,
    image_id    integer references images(id),
    user_id     integer references users (id)
);

-- changeSet SepMari:1
create table if not exists comments
(
    id                serial primary key not null,
    author_id         integer references users (id),
    created_at        timestamp,
    advert_id         integer references adverts (id),
    text              text               not null
    );


--changeSet akmeevd:3
create table authorities(
    username varchar(100),
    authority varchar(20)
);

