create table participant
(
    identifier varchar(255) not null,
    project_id varchar(255) not null,
    user_id varchar(255) not null,
    version bigint not null,
    primary key (identifier)
);
