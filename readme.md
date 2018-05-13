This project contains helper classes to call stored procedures and functions. The base framework is spring (spring-jdbc).
It is fair-typesafe (no unsafe casts inside). Most compatible database is Oracle.

# Oracle Examples

For Oracle procedure/function calls you can use NamedJdbcCall. The parameters are passing by name, that allows:
* parameters reordering
* support default value
* support overloading

Consider you have a procedure and a function:

```oraclesqlplus
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
```

You can call it:

```java
import org.morejdbc.*;
import static org.morejdbc.SqlTypes.*;

...
private JdbcTemplate jdbcTemplate;

...
Out<Integer> sum = Out.of(INTEGER);
Out<Long> mlt = Out.of(BIGINT);
jdbcTemplate.execute(call("test_math")
        .in("val1", 1)
        .in("val2", 2L)
        .out("out_sum", sum)
        .out("out_mlt", mlt)
);
// sum.get() is 3 (Integer)
// mlt.get() is 2L (Long)

```

or in value-consumer style with lambdas

```java
AtomicReference<Integer> sum = new AtomicReference<>();
AtomicReference<Long> mlt = new AtomicReference<>();
jdbcTemplate.execute(call("test_math")
        .in("val1", 1)
        .in("val2", 2L)
        .out("out_sum", INTEGER, sum::set)
        .out("out_mlt", BIGINT, mlt::set)
);
// sum.get() is 3 (Integer)
// mlt.get() is 2L (Long)
```

For functions:

```java
String result = jdbcTemplate.execute(call("get_concat", VARCHAR)
        .in("s2", "def")
        .in("s1", "abc") // note: reordered s1, s2
);
// result is "abcdef" (s1 || s2)
```

