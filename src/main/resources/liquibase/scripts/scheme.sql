-- liquibase formatted sql

-- changeSet 11th:1
create table if not exists users
(
    id         serial primary key not null,
    password   text               not null,
    first_name varchar(70)        not null,
    last_name  varchar(70)        not null,
    email      varchar(50) unique not null,
    phone      varchar(15),
    photo      text,
    role       varchar(10)
);

create table if not exists adverts
(
    id          serial primary key not null,
    title       varchar(100)       not null,
    description text,
    price       integer,
    photo       text,
    user_id     integer references users (id)
);

-- changeSet SepMari:1
create table if not exists comments
(
    id                serial primary key not null,
    author_id         integer references users (id),
    created_at        integer,
    pk_id             integer references adverts (id),
    text              text               not null
    );

-- changeSet akmeevd:1
alter table comments drop column pk_id;
alter table comments add column advert_id integer references adverts (id);

-- changeSet 11th:2
alter table comments drop column created_at;
alter table comments add column created_at timestamp;

-- changeSet 11th:3
create table if not exists photos
(
    id              serial primary key not null,
    photo_type      varchar(10),
    photo_dir       varchar(50),
    file_type       varchar(30),
    file_name       varchar(100),
    file_extension  varchar(10),
    file_size       bigint
);
alter table adverts drop column photo;
alter table adverts add column image_id integer references photos(id);

--changeSet akmeevd:2
alter table users add column avatar_id integer references photos (id);

--changeSet akmeevd:3
create table authorities(
    username varchar(100),
    authority varchar(20)
);

-- changeSet 11th:4
alter table users drop column email,
                  drop column photo,
                  add column username varchar(50) unique,
                  add column enabled bool,
                  alter column first_name drop not null,
                  alter column last_name drop not null;

-- changeSet 11th:5
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
alter table users drop column avatar_id;
alter table users add column image_id integer references images(id);
alter table adverts drop column image_id;
alter table adverts add column image_id integer references images(id);
drop table photos;