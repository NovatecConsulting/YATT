alter table if exists tasks
    add column assignee_company_name varchar (255);

alter table if exists tasks
    add column assignee_first_name varchar (255);

alter table if exists tasks
    add column assignee_last_name varchar (255);