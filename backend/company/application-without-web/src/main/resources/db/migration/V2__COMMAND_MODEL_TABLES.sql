create table employee_unique_key
(
    identifier varchar(255) not null,
    company_id varchar(255) not null,
    user_id    varchar(255) not null,
    primary key (identifier)
);

alter table if exists employee_unique_key
    add constraint UKikkx651fvk6yi9iymk7620mgp unique (company_id, user_id);