create table task_todos
(
    task_identifier varchar(255) not null,
    description     varchar(255) not null,
    is_done         boolean      not null,
    identifier      varchar(255)
);

alter table if exists task_todos
    add constraint UK_TaskTodos_Identifier unique (identifier, task_identifier);

alter table if exists task_todos
    add constraint FK_TaskTodos_TaskIdentifier
        foreign key (task_identifier)
            references tasks;

create index IX_TaskTodo_TaskIden on task_todos (task_identifier);