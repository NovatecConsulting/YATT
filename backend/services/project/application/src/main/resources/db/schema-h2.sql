create sequence hibernate_sequence start with 1 increment by 1;

    create table association_value_entry (
       id bigint not null,
        association_key varchar(255) not null,
        association_value varchar(255),
        saga_id varchar(255) not null,
        saga_type varchar(255),
        primary key (id)
    );

    create table domain_event_entry (
       global_index bigint not null,
        event_identifier varchar(255) not null,
        meta_data blob,
        payload blob not null,
        payload_revision varchar(255),
        payload_type varchar(255) not null,
        time_stamp varchar(255) not null,
        aggregate_identifier varchar(255) not null,
        sequence_number bigint not null,
        type varchar(255),
        primary key (global_index)
    );

    create table participant (
       identifier varchar(255) not null,
        company_id varchar(255) not null,
        company_name varchar(255),
        project_id varchar(255) not null,
        user_email varchar(255),
        user_first_name varchar(255),
        user_id varchar(255) not null,
        user_last_name varchar(255),
        user_telephone varchar(255),
        version bigint not null,
        primary key (identifier)
    );

    create table participant_unique_key (
       identifier varchar(255) not null,
        company_id varchar(255) not null,
        project_id varchar(255) not null,
        user_id varchar(255) not null,
        primary key (identifier)
    );

    create table project_acls (
       aggregate_identifier varchar(255) not null,
        aggregate_type varchar(255) not null,
        permission varchar(255) not null,
        identifier varchar(255) not null,
        primary key (aggregate_identifier, aggregate_type, permission, identifier)
    );

    create table project_by_task_lookup (
       identifier varchar(255) not null,
        project_id varchar(255) not null,
        primary key (identifier)
    );

    create table project_details (
       identifier varchar(255) not null,
        all_tasks_count bigint not null,
        completed_tasks_count bigint not null,
        deadline date not null,
        name varchar(255) not null,
        planned_start_date date not null,
        planned_tasks_count bigint not null,
        started_tasks_count bigint not null,
        version bigint not null,
        primary key (identifier)
    );

    create table projects (
       identifier varchar(255) not null,
        actual_end_date date,
        company_name varchar(255),
        company_id varchar(255) not null,
        deadline date not null,
        name varchar(255) not null,
        planned_start_date date not null,
        status varchar(255) not null,
        version bigint not null,
        primary key (identifier)
    );

    create table root_context_id_mapping (
       aggregate_identifier varchar(255) not null,
        aggregate_type varchar(255) not null,
        root_context_id varchar(255) not null,
        primary key (aggregate_identifier, aggregate_type, root_context_id)
    );

    create table saga_entry (
       saga_id varchar(255) not null,
        revision varchar(255),
        saga_type varchar(255),
        serialized_saga blob,
        primary key (saga_id)
    );

    create table snapshot_event_entry (
       aggregate_identifier varchar(255) not null,
        sequence_number bigint not null,
        type varchar(255) not null,
        event_identifier varchar(255) not null,
        meta_data blob,
        payload blob not null,
        payload_revision varchar(255),
        payload_type varchar(255) not null,
        time_stamp varchar(255) not null,
        primary key (aggregate_identifier, sequence_number, type)
    );

    create table task_schedule_projection (
       identifier varchar(255) not null,
        end_date date not null,
        project_id varchar(255) not null,
        start_date date not null,
        primary key (identifier)
    );

    create table task_todos (
       task_identifier varchar(255) not null,
        description varchar(255) not null,
        is_done boolean not null,
        identifier varchar(255)
    );

    create table tasks (
       identifier varchar(255) not null,
        description varchar(255),
        end_date date not null,
        name varchar(255) not null,
        participant_id varchar(255),
        project_id varchar(255) not null,
        start_date date not null,
        status integer not null,
        version bigint not null,
        primary key (identifier)
    );

    create table token_entry (
       processor_name varchar(255) not null,
        segment integer not null,
        owner varchar(255),
        timestamp varchar(255) not null,
        token blob,
        token_type varchar(255),
        primary key (processor_name, segment)
    );
create index IDXk45eqnxkgd8hpdn6xixn8sgft on association_value_entry (saga_type, association_key, association_value);
create index IDXgv5k1v2mh6frxuy5c0hgbau94 on association_value_entry (saga_id, saga_type);

    alter table domain_event_entry 
       add constraint UK8s1f994p4la2ipb13me2xqm1w unique (aggregate_identifier, sequence_number);

    alter table domain_event_entry 
       add constraint UK_fwe6lsa8bfo6hyas6ud3m8c7x unique (event_identifier);

    alter table participant_unique_key 
       add constraint participant_unique_key_constraint unique (project_id, company_id, user_id);

    alter table snapshot_event_entry 
       add constraint UK_e1uucjseo68gopmnd0vgdl44h unique (event_identifier);

    alter table task_todos 
       add constraint UK_TaskTodos_Identifier unique (identifier, task_identifier);

    alter table task_todos 
       add constraint FK_TaskTodos_TaskIdentifier 
       foreign key (task_identifier) 
       references tasks;

create index IX_TaskTodo_TaskIden on task_todos (task_identifier);

