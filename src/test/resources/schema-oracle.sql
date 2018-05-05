create table TEMP_PK_TRIGGER (
  ID    NUMBER not null primary key,
  VALUE VARCHAR2(400 char)
);

create sequence TEMP_PK_TRIGGER_SEQ;

create or replace trigger TEMP_PK_TRIGGER_BI
  before insert
  on TEMP_PK_TRIGGER
  for each row
  begin
    :new.id := temp_pk_trigger_seq.nextval;
  end;

CREATE PROCEDURE test_math(
  val1    IN  number,
  val2        number,
  out_sum OUT number,
  out_mlt OUT number
) IS
  BEGIN
    out_sum := val1 + val2;
    out_mlt := val1 * val2;
  END;

CREATE FUNCTION get_concat(s1 varchar2, s2 varchar2)
  RETURN VARCHAR2
IS
  BEGIN
    RETURN s1 || s2;
  END;

CREATE FUNCTION simple_decode(p_str varchar2)
  return varchar2
as
  v_str varchar2(4000 char);
  begin
    v_str := replace(p_str, '%3D', '=');
    v_str := replace(v_str, '%0A', chr(10));
    v_str := replace(v_str, '%0D', chr(13));
    v_str := replace(v_str, '%3B', ';');
    v_str := replace(v_str, '%7C', '|');
    v_str := replace(v_str, '%25', '%');

    return v_str;
  end simple_decode;

CREATE FUNCTION get_extras_tab(extra_string varchar2)
  return sys_refcursor
is
  v_cur sys_refcursor;
  begin
    open v_cur for
    select
      simple_decode(regexp_replace(pair, '([^=]+)(=)(.+)', '\1')) id,
      simple_decode(regexp_replace(pair, '([^=]+)(=)(.+)', '\3')) value
    from (
      select regexp_substr(extra_string, '[^;]+', 1, level)
        as pair
      from dual
      connect by instr(extra_string, ';', 1, level) > 0
    );

    return v_cur;
  end;

CREATE FUNCTION blobs_concat(b1 blob, b2 blob)
  return blob
is
  b1_copy blob := b1;
  begin
    dbms_lob.append(b1_copy, b2);
    return b1_copy;
  end;

CREATE PROCEDURE test_in_out(x in number, y number, io_sum in out number)
is
  begin
    io_sum := x + y + io_sum;
  end;

