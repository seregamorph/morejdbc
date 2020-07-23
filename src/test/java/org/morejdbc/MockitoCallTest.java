package org.morejdbc;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.morejdbc.JdbcCall.callSql;
import static org.morejdbc.NamedJdbcCall.call;
import static org.morejdbc.OracleSqlTypes.cursor;
import static org.morejdbc.SqlTypes.*;
import static org.morejdbc.TestUtils.immutableEntry;

public class MockitoCallTest {

    @Test
    public void testCallSqlMock() {
        var jdbc = mock(JdbcTemplate.class);
        var sum = MockOut.of(INTEGER);
        var mlt = MockOut.of(INTEGER);
        when(jdbc.execute(callSql("{call test_math(?, ?, ?, ?)}").in(10).in(20).out(sum).out(mlt))).then(invocation -> {
            sum.setTo(invocation.getArguments()[0], 30);
            mlt.setTo(invocation.getArguments()[0], 200);
            return null;
        });
        var result = serviceCallSql(jdbc, 10, 20);
        assertEquals(30, result.sum);
        assertEquals(200, result.mlt);
    }

    @Test
    public void testCallNamedMock() {
        var jdbc = mock(JdbcTemplate.class);
        var sum = MockOut.of(INTEGER);
        var mlt = MockOut.of(BIGINT);
        when(jdbc.execute(call("test_math").in("val1", 10).in("val2", 20).out("out_sum", sum).out("out_mlt", mlt))).then(invocation -> {
            sum.setTo(invocation.getArguments()[0], 30);
            mlt.setTo(invocation.getArguments()[0], 200L);
            return null;
        });
        var result = serviceCallNamed(jdbc, 10, 20);
        assertEquals(30, result.sum);
        assertEquals(200, result.mlt);
    }

    @Test
    public void testCallNamedFunctionMock() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.execute(call("get_concat", VARCHAR).in("s2", "def").in("s1", 4))).thenReturn("4def");
        var result = serviceCallNamedFunction(jdbc, 4, "def");
        assertEquals("4def", result);
    }

    @Test
    public void testRefCursorOutParam() {
        var jdbc = mock(JdbcTemplate.class);
        var out = MockOut.of(cursor((rs, rowNum) -> immutableEntry("key", "value")));
        when(jdbc.execute(call("proc_extras_tab").in("extra_string", "1=value1;2=value2;6=value6;").out("v_cur", out))).then(invocation -> {
            out.setTo(invocation.getArguments()[0], Arrays.asList(immutableEntry("1", "value1"), immutableEntry("2", "value2"), immutableEntry("6", "value6")));
            return null;
        });
        var extras = serviceTestRefCursorOutParam(jdbc, "1=value1;2=value2;6=value6;");
        assertEquals(Arrays.asList(immutableEntry("1", "value1"), immutableEntry("2", "value2"), immutableEntry("6", "value6")), extras);
    }

    private static List<Map.Entry<String, String>> serviceTestRefCursorOutParam(JdbcTemplate jdbc, String extra) {
        var outExtras = Out.of(cursor((rs, rowNum) -> immutableEntry(rs.getString("id"), rs.getString("value"))));
        jdbc.execute(call("proc_extras_tab").in("extra_string", extra).out("v_cur", outExtras));
        return outExtras.get();
    }

    private static Result serviceCallSql(JdbcTemplate jdbc, int val1, int val2) {
        var sum = Out.of(INTEGER);
        var mlt = Out.of(INTEGER);

        jdbc.execute(callSql("{call test_math(?, ?, ?, ?)}")
                .in(val1)
                .in(val2)
                .out(sum)
                .out(mlt));

        return new Result(sum.get(), mlt.get());
    }

    private static Result serviceCallNamed(JdbcTemplate jdbc, int val1, int val2) {
        var sum = new AtomicReference<Integer>();
        var mlt = new AtomicReference<Long>();

        jdbc.execute(call("test_math")
                .in("val1", val1)
                .in("val2", val2)
                .out("out_sum", INTEGER, sum::set)
                .out("out_mlt", BIGINT, mlt::set));

        return new Result(sum.get(), mlt.get());
    }

    private static String serviceCallNamedFunction(JdbcTemplate jdbc, int arg1, String arg2) {
        return jdbc.execute(call("get_concat", VARCHAR)
                .in("s2", arg2)
                .in("s1", arg1));
    }

    private static class Result {

        private final int sum;
        private final long mlt;

        private Result(int sum, long mlt) {
            this.sum = sum;
            this.mlt = mlt;
        }
    }
}
