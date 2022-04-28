create table user_unique_key
(
    identifier       varchar(255) not null,
    email            varchar(255) not null,
    external_user_id varchar(255) not null,
    primary key (identifier)
);

alter table if exists user_unique_key
    add constraint UK_7rl1lg6v0c2b4os3hd99hy0un unique (email);

alter table if exists user_unique_key
    add constraint UK_hbn7ykwddd2spkc6buayh0lm1 unique (external_user_id);


