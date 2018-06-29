--liquibase formatted sql

--changeset seregamorph:FEA-0-create-user-1
CREATE USER test
IDENTIFIED BY test
  DEFAULT TABLESPACE users;

ALTER USER test
quota 100M on users;

GRANT
CREATE SESSION,
CREATE SEQUENCE,
CREATE TABLE,
CREATE TRIGGER,
CREATE PROCEDURE
TO test;
--rollback not required

