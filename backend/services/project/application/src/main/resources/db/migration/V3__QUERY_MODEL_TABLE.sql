create table projects
(
    identifier         varchar(255) not null,
    actual_end_date    date,
    company_name       varchar(255),
    company_id         varchar(255) not null,
    deadline           date         not null,
    name               varchar(255) not null,
    planned_start_date date         not null,
    status             varchar(255) not null,
    version            int8         not null,
    primary key (identifier)
);

create table project_details
(
    identifier            varchar(255) not null,
    all_tasks_count       int8         not null,
    completed_tasks_count int8         not null,
    deadline              date         not null,
    name                  varchar(255) not null,
    planned_start_date    date         not null,
    planned_tasks_count   int8         not null,
    started_tasks_count   int8         not null,
    version               int8         not null,
    primary key (identifier)
);

create table project_by_task_lookup
(
    identifier varchar(255) not null,
    project_id varchar(255) not null,
    primary key (identifier)
);

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