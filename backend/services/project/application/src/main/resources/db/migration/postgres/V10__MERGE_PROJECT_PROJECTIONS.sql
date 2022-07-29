drop table project_details;

alter table projects
    add column all_tasks_count int8 not null,
    add column completed_tasks_count int8 not null,
    add column planned_tasks_count   int8 not null,
    add column started_tasks_count   int8 not null;