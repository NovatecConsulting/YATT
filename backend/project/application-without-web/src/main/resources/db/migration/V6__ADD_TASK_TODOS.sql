create table task_projection_todos
(
    task_projection_identifier varchar(255) not null,
    description                varchar(255),
    is_done                    boolean      not null,
    todo_id                    varchar(255) not null
);

alter table if exists task_projection_todos
    add constraint FK_task_projection_identifier
    foreign key (task_projection_identifier)
    references tasks;

create index IX_task_todos_task_projection_identifier on task_projection_todos (task_projection_identifier);