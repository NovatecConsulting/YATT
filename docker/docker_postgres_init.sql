CREATE USER userapp WITH PASSWORD 'axon' CREATEDB;
CREATE DATABASE userdb
    WITH
    OWNER = userapp
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

CREATE USER companyapp WITH PASSWORD 'axon' CREATEDB;
CREATE DATABASE companydb
    WITH
    OWNER = companyapp
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

CREATE USER projectapp WITH PASSWORD 'axon' CREATEDB;
CREATE DATABASE projectdb
    WITH
    OWNER = projectapp
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;