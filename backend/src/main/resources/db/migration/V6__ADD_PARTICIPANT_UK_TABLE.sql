create table participant_unique_key
(
    identifier varchar(255) not null,
    company_id varchar(255) not null,
    project_id varchar(255) not null,
    user_id    varchar(255) not null,
    primary key (identifier)
);

alter table if exists participant_unique_key
    add constraint UKlck0qqqdij8b3cchkj8u9tjis unique (company_id, user_id);
