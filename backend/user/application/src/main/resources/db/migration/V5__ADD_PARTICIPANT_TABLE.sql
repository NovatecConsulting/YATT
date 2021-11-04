create table participant
(
    identifier      varchar(255) not null,
    company_id      varchar(255) not null,
    company_name    varchar(255),
    project_id      varchar(255) not null,
    user_first_name varchar(255),
    user_id         varchar(255) not null,
    user_last_name  varchar(255),
    version         int8         not null,
    primary key (identifier)
);
