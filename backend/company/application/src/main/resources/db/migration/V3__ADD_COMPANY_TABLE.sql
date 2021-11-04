create table company
(
    identifier varchar(255) not null,
    name       varchar(255) not null,
    version    int8         not null,
    primary key (identifier)
);