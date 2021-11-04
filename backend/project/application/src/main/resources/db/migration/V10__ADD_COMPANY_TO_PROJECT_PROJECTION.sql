alter table projects
    add column company_name varchar(255);

alter table projects
    add column company_id varchar(255) not null;