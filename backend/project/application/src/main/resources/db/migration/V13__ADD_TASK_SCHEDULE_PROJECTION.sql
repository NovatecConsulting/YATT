create table task_schedule_projection
(
    identifier varchar(255) not null,
    end_date   date         not null,
    project_id varchar(255) not null,
    start_date date         not null,
    primary key (identifier)
);