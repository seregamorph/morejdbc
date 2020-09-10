
CREATE OR REPLACE FUNCTION hi_lo(
      a  NUMERIC,
      b  NUMERIC,
      c  NUMERIC,
  OUT hi NUMERIC,
  OUT lo NUMERIC)
AS $$
BEGIN
  hi := GREATEST(a, b, c);
  lo := LEAST(a, b, c);
END; $$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION refcursorfunc()
  RETURNS refcursor AS $$
DECLARE
  mycurs refcursor;
BEGIN
  OPEN mycurs FOR SELECT 1
                  UNION SELECT 2;
  RETURN mycurs;
END; $$
LANGUAGE plpgsql;
