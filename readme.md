[![license](https://img.shields.io/badge/License-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0)
[![build](https://travis-ci.com/dmvolodin/morejdbc.svg?branch=master)](https://travis-ci.com/github/dmvolodin/morejdbc)



This project contains helper classes to call stored procedures and functions. The base framework is spring (spring-jdbc).
It is fair-typesafe (no unsafe casts inside). The most compatible database is Oracle.

To add the library in Maven:
```xml
<dependency>
    <groupId>com.github.seregamorph</groupId>
    <artifactId>morejdbc</artifactId>
    <version>1.0</version>
</dependency>
```
For gradle: `compile com.github.seregamorph:morejdbc:1.0`

# Oracle Examples

For Oracle procedure/function calls you can use NamedJdbcCall. The parameters are passing by name, that allows:
* parameters reordering
* support default value
* support overloading

Consider you have a procedure and a function in a package:

```sql
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
end;
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
jdbcTemplate.execute(call("test_more_jdbc_pkg.calc_sum_and_multiply_of_two_numbers")
        .in("p_number1", 1)
        .in("p_number2", 2L)
        .out("po_sum", sum)
        .out("po_mlt", mlt)
);
// sum.get() is 3 (Integer)
// mlt.get() is 2L (Long)

```

or in value-consumer style with lambdas

```java
AtomicReference<Integer> sum = new AtomicReference<>();
AtomicReference<Long> mlt = new AtomicReference<>();
jdbcTemplate.execute(call("test_more_jdbc_pkg.calc_sum_and_multiply_of_two_numbers")
        .in("p_number1", 1)
        .in("p_number2", 2L)
        .out("po_sum", INTEGER, sum::set)
        .out("po_mlt", BIGINT, mlt::set)
);
// sum.get() is 3 (Integer)
// mlt.get() is 2L (Long)
```

For functions:

```java
String result = jdbcTemplate.execute(call("test_more_jdbc_pkg.get_concat_of_two_strings", VARCHAR)
        .in("p_string2", "def")
        .in("p_string1", "abc") // note: reordered p_string1, p_string2
);
// result is "abcdef" (p_string1 + p_string2)
```

