create table project_by_task_lookup
(
    identifier varchar(255) not null,
    project_id varchar(255) not null,
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
