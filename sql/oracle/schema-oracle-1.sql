--liquibase formatted sql

--changeset seregamorph:FEA-1-create-schema-1
create table table_with_identity_pk
(
    id    number generated always as identity (start with 1 increment by 1) primary key,
    value varchar2(400 char)
)
/

--changeset seregamorph:FEA-1-create-schema-2
create or replace package test_more_jdbc_pkg
as
    procedure calc_sum_and_multiply_of_two_numbers(p_number1 number,
                                                   p_number2 number,
                                                   po_sum out number,
                                                   po_mlt out number);

    function get_concat_of_two_strings(p_string1 varchar2, p_string2 varchar2)
        return varchar2;

    function get_simple_decoded_string(p_string varchar2)
        return varchar2;

    function get_cursor_from_key_value_as_string(p_key_value_string varchar2)
        return sys_refcursor;

    procedure get_cursor_from_key_value_as_string(p_key_value_string varchar2, po_cursor out sys_refcursor);

    function get_two_blobs_concatenated(p_blob1 blob, p_blob2 blob)
        return blob;

    procedure calc_sum_of_two_numbers_with_in_out_parameter(p_number1 number,
                                                            p_number2 number,
                                                            pio_sum in out number);
end;
/

--changeset seregamorph:FEA-1-create-schema-3
create or replace package body test_more_jdbc_pkg
as
    procedure calc_sum_and_multiply_of_two_numbers(p_number1 number,
                                                   p_number2 number,
                                                   po_sum out number,
                                                   po_mlt out number)
        is
    begin
        po_sum := p_number1 + p_number2;
        po_mlt := p_number1 * p_number2;
    end;

    function get_concat_of_two_strings(p_string1 varchar2, p_string2 varchar2)
        return varchar2 is
    begin
        return p_string1 || p_string2;
    end;

    function get_simple_decoded_string(p_string varchar2)
        return varchar2 is
        v_result varchar2(4000 char);
    begin
        v_result := replace(p_string, '%3D', '=');
        v_result := replace(v_result, '%0A', chr(10));
        v_result := replace(v_result, '%0D', chr(13));
        v_result := replace(v_result, '%3B', ';');
        v_result := replace(v_result, '%7C', '|');
        v_result := replace(v_result, '%25', '%');

        return v_result;
    end;

    function get_cursor_from_key_value_as_string(p_key_value_string varchar2)
        return sys_refcursor is
        v_result sys_refcursor;
    begin
        open v_result for
            select get_simple_decoded_string(regexp_replace(pair, '([^=]+)(=)(.+)', '\1')) id,
                   get_simple_decoded_string(regexp_replace(pair, '([^=]+)(=)(.+)', '\3')) value
            from (
                     select regexp_substr(p_key_value_string, '[^;]+', 1, level) as pair
                     from dual
                     connect by instr(p_key_value_string, ';', 1, level) > 0
                 );

        return v_result;
    end;

    procedure get_cursor_from_key_value_as_string(p_key_value_string varchar2, po_cursor out sys_refcursor)
        is
    begin
        po_cursor := get_cursor_from_key_value_as_string(p_key_value_string);
    end;

    function get_two_blobs_concatenated(p_blob1 blob, p_blob2 blob)
        return blob is
        v_result blob := p_blob1;
    begin
        dbms_lob.append(v_result, p_blob2);
        return v_result;
    end;

    procedure calc_sum_of_two_numbers_with_in_out_parameter(p_number1 number,
                                                            p_number2 number,
                                                            pio_sum in out number)
        is
    begin
        pio_sum := p_number1 + p_number2 + pio_sum;
    end;
end;
/
