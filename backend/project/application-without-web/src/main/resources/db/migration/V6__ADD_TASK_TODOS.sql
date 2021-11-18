create table task_todos
(
    task_id     varchar(255) not null,
    todo_id     varchar(255) not null,
    description varchar(255),
    is_done     boolean      not null,
    primary key (task_id, todo_id)
);

alter table if exists task_todos
    add constraint FK_TaskTodo_taskId
        foreign key (task_id)
            references tasks;

create index IX_TaskTodo_Task on task_todos (task_id);