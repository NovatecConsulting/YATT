create table users
(
    identifier       varchar(255) not null,
    external_user_id varchar(255) not null,
    firstname        varchar(255) not null,
    lastname         varchar(255) not null,
    primary key (identifier)
);