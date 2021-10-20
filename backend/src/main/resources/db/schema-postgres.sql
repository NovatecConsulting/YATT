create sequence hibernate_sequence start 1 increment 1;

    create table association_value_entry (
       id int8 not null,
        association_key varchar(255) not null,
        association_value varchar(255),
        saga_id varchar(255) not null,
        saga_type varchar(255),
        primary key (id)
    );

    create table company (
       identifier varchar(255) not null,
        name varchar(255) not null,
        version int8 not null,
        primary key (identifier)
    );

    create table domain_event_entry (
       global_index int8 not null,
        event_identifier varchar(255) not null,
        meta_data oid,
        payload oid not null,
        payload_revision varchar(255),
        payload_type varchar(255) not null,
        time_stamp varchar(255) not null,
        aggregate_identifier varchar(255) not null,
        sequence_number int8 not null,
        type varchar(255),
        primary key (global_index)
    );

    create table employee (
       identifier varchar(255) not null,
        company_id varchar(255) not null,
        is_admin boolean not null,
        is_project_manager boolean not null,
        user_first_name varchar(255),
        user_id varchar(255) not null,
        user_last_name varchar(255),
        version int8 not null,
        primary key (identifier)
    );

    create table employee_unique_key (
       identifier varchar(255) not null,
        company_id varchar(255) not null,
        user_id varchar(255) not null,
        primary key (identifier)
    );

    create table participant (
       identifier varchar(255) not null,
        company_id varchar(255) not null,
        company_name varchar(255),
        project_id varchar(255) not null,
        user_first_name varchar(255),
        user_id varchar(255) not null,
        user_last_name varchar(255),
        version int8 not null,
        primary key (identifier)
    );

    create table participant_unique_key (
       identifier varchar(255) not null,
        company_id varchar(255) not null,
        project_id varchar(255) not null,
        user_id varchar(255) not null,
        primary key (identifier)
    );

    create table project_by_task_lookup (
       identifier varchar(255) not null,
        project_id varchar(255) not null,
        primary key (identifier)
    );

    create table project_details (
       identifier varchar(255) not null,
        all_tasks_count int8 not null,
        completed_tasks_count int8 not null,
        deadline date not null,
        name varchar(255) not null,
        planned_start_date date not null,
        planned_tasks_count int8 not null,
        started_tasks_count int8 not null,
        version int8 not null,
        primary key (identifier)
    );

    create table projects (
       identifier varchar(255) not null,
        deadline date not null,
        name varchar(255) not null,
        planned_start_date date not null,
        version int8 not null,
        primary key (identifier)
    );

    create table saga_entry (
       saga_id varchar(255) not null,
        revision varchar(255),
        saga_type varchar(255),
        serialized_saga oid,
        primary key (saga_id)
    );

    create table snapshot_event_entry (
       aggregate_identifier varchar(255) not null,
        sequence_number int8 not null,
        type varchar(255) not null,
        event_identifier varchar(255) not null,
        meta_data oid,
        payload oid not null,
        payload_revision varchar(255),
        payload_type varchar(255) not null,
        time_stamp varchar(255) not null,
        primary key (aggregate_identifier, sequence_number, type)
    );

    create table tasks (
       identifier varchar(255) not null,
        description varchar(255),
        end_date date not null,
        name varchar(255) not null,
        project_id varchar(255) not null,
        start_date date not null,
        status int4 not null,
        version int8 not null,
        primary key (identifier)
    );

    create table token_entry (
       processor_name varchar(255) not null,
        segment int4 not null,
        owner varchar(255),
        timestamp varchar(255) not null,
        token oid,
        token_type varchar(255),
        primary key (processor_name, segment)
    );

    create table user_unique_key (
       identifier varchar(255) not null,
        external_user_id varchar(255) not null,
        primary key (identifier)
    );

    create table users (
       identifier varchar(255) not null,
        external_user_id varchar(255) not null,
        firstname varchar(255) not null,
        lastname varchar(255) not null,
        primary key (identifier)
    );
create index IDXk45eqnxkgd8hpdn6xixn8sgft on association_value_entry (saga_type, association_key, association_value);
create index IDXgv5k1v2mh6frxuy5c0hgbau94 on association_value_entry (saga_id, saga_type);

    alter table if exists domain_event_entry 
       add constraint UK8s1f994p4la2ipb13me2xqm1w unique (aggregate_identifier, sequence_number);

    alter table if exists domain_event_entry 
       add constraint UK_fwe6lsa8bfo6hyas6ud3m8c7x unique (event_identifier);

    alter table if exists employee_unique_key 
       add constraint UKikkx651fvk6yi9iymk7620mgp unique (company_id, user_id);

    alter table if exists participant_unique_key 
       add constraint UKlck0qqqdij8b3cchkj8u9tjis unique (company_id, user_id);

    alter table if exists snapshot_event_entry 
       add constraint UK_e1uucjseo68gopmnd0vgdl44h unique (event_identifier);

    alter table if exists user_unique_key 
       add constraint UK_hbn7ykwddd2spkc6buayh0lm1 unique (external_user_id);
