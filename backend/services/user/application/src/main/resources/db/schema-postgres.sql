create sequence hibernate_sequence start 1 increment 1;

    create table association_value_entry (
       id int8 not null,
        association_key varchar(255) not null,
        association_value varchar(255),
        saga_id varchar(255) not null,
        saga_type varchar(255),
        primary key (id)
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
        email varchar(255) not null,
        external_user_id varchar(255) not null,
        primary key (identifier)
    );

    create table users (
       identifier varchar(255) not null,
        email varchar(255) not null,
        external_user_id varchar(255) not null,
        firstname varchar(255) not null,
        lastname varchar(255) not null,
        telephone varchar(255) not null,
        primary key (identifier)
    );
create index IDXk45eqnxkgd8hpdn6xixn8sgft on association_value_entry (saga_type, association_key, association_value);
create index IDXgv5k1v2mh6frxuy5c0hgbau94 on association_value_entry (saga_id, saga_type);

    alter table if exists domain_event_entry 
       add constraint UK8s1f994p4la2ipb13me2xqm1w unique (aggregate_identifier, sequence_number);

    alter table if exists domain_event_entry 
       add constraint UK_fwe6lsa8bfo6hyas6ud3m8c7x unique (event_identifier);

    alter table if exists snapshot_event_entry 
       add constraint UK_e1uucjseo68gopmnd0vgdl44h unique (event_identifier);

    alter table if exists user_unique_key 
       add constraint UK_7rl1lg6v0c2b4os3hd99hy0un unique (email);

    alter table if exists user_unique_key 
       add constraint UK_hbn7ykwddd2spkc6buayh0lm1 unique (external_user_id);
