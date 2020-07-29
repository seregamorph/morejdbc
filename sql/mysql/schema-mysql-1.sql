-- liquibase formatted sql

-- changeset seregamorph:FEA-1-create-schema-1 splitStatements:false
delimiter //
create procedure test_math(IN val1 int, IN val2 int, OUT out_sum int, OUT out_mlt int)
begin
    set out_sum = val1 + val2;
    set out_mlt = val1 * val2;
end;
//

-- changeset seregamorph:FEA-1-create-schema-2 splitStatements:false
create function get_concat(s1 varchar(50), s2 varchar(50)) returns varchar(100)
    return concat(s1, s2);
