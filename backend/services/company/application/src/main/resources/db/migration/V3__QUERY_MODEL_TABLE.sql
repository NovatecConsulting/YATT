create table company
(
    identifier varchar(255) not null,
    name       varchar(255) not null,
    version    int8         not null,
    primary key (identifier)
);

create table employee
(
    identifier         varchar(255) not null,
    company_id         varchar(255) not null,
    is_admin           boolean      not null,
    is_project_manager boolean      not null,
    user_first_name    varchar(255),
    user_id            varchar(255) not null,
    user_last_name     varchar(255),
    version            int8         not null,
    primary key (identifier)
);