-- liquibase formatted sql

-- changeset seregamorph:FEA-1-create-schema-1 splitStatements:false
CREATE PROCEDURE test_math(
  IN  val1    int,
  IN  val2    int,
  OUT out_sum int,
  OUT out_mlt int
)
  BEGIN
    set out_sum = val1 + val2;
    set out_mlt = val1 * val2;
  END;

-- changeset seregamorph:FEA-1-create-schema-2 splitStatements:false
CREATE FUNCTION get_concat(s1 varchar(50), s2 varchar(50))
  RETURNS VARCHAR(100) DETERMINISTIC
  RETURN concat(s1, s2);

