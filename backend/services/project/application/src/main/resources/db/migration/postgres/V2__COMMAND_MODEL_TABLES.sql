create table participant_unique_key
(
    identifier varchar(255) not null,
    company_id varchar(255) not null,
    project_id varchar(255) not null,
    user_id    varchar(255) not null,
    primary key (identifier)
);

alter table if exists participant_unique_key
    add constraint participant_unique_key_constraint unique (project_id, company_id, user_id);

create table task_schedule_projection
(
    identifier varchar(255) not null,
    end_date   date         not null,
    project_id varchar(255) not null,
    start_date date         not null,
    primary key (identifier)
);

