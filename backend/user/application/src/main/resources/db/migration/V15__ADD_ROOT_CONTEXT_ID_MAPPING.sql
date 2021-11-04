create table root_context_id_mapping
(
    aggregate_identifier varchar(255) not null,
    aggregate_type       varchar(255) not null,
    root_context_id      varchar(255) not null,
    primary key (aggregate_identifier, aggregate_type, root_context_id)
);