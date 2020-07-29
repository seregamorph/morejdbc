--liquibase formatted sql

--changeset seregamorph:fea-1-create-schema-1 splitstatements:false
create or replace function hi_lo(
      a  numeric,
      b  numeric,
      c  numeric,
  out hi numeric,
  out lo numeric)
as $$
begin
  hi := greatest(a, b, c);
  lo := least(a, b, c);
end; $$
language plpgsql;

--changeset seregamorph:fea-1-create-schema-2 splitstatements:false
create or replace function refcursorfunc()
  returns refcursor as $$
declare
  mycurs refcursor;
begin
  open mycurs for select 1
                  union select 2;
  return mycurs;
end; $$
language plpgsql;
