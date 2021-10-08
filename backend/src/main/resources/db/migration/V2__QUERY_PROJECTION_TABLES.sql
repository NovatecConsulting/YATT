create table projects
(
    identifier         varchar(255) not null,
    deadline           date         not null,
    name               varchar(255) not null,
    planned_start_date date         not null,
    version            int8         not null,
    primary key (identifier)
);

create table tasks
(
    identifier  varchar(255) not null,
    description varchar(255),
    end_date    date         not null,
    name        varchar(255) not null,
    project_id  varchar(255) not null,
    start_date  date         not null,
    status      int4         not null,
    version     int8         not null,
    primary key (identifier)
);

create table users
(
    identifier       varchar(255) not null,
    external_user_id varchar(255) not null,
    firstname        varchar(255) not null,
    lastname         varchar(255) not null,
    primary key (identifier)
);