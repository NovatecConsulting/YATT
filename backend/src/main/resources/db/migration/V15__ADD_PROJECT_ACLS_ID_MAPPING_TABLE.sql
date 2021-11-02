create table project_acls_id_mapping
(
    aggregate_identifier varchar(255) not null,
    aggregate_type       varchar(255) not null,
    identifier           varchar(255) not null,
    primary key (aggregate_identifier, aggregate_type, identifier)
);
