package org.morejdbc;

import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.morejdbc.JdbcCall.callSql;
import static org.morejdbc.NamedJdbcCall.call;
import static org.morejdbc.OracleSqlTypes.cursor;
import static org.morejdbc.SqlTypes.BIGINT;
import static org.morejdbc.SqlTypes.INTEGER;
import static org.morejdbc.SqlTypes.VARCHAR;
import static org.morejdbc.TestUtils.immutableEntry;

public class MockitoCallTest {

    @Test
    public void testCallSqlMock() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        MockOut<Integer> sum = MockOut.of(INTEGER);
        MockOut<Integer> mlt = MockOut.of(INTEGER);
        when(jdbc.execute(callSql("{call test_math(?, ?, ?, ?)}")
                .in(10).in(20).out(sum).out(mlt))).then(invocation -> {
            sum.setTo(invocation.getArguments()[0], 30);
            mlt.setTo(invocation.getArguments()[0], 200);
            return null;
        });

        Result result = serviceCallSql(jdbc, 10, 20);

        assertEquals(30, result.sum);
        assertEquals(200, result.mlt);
    }

    @Test
    public void testCallNamedMock() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        MockOut<Integer> sum = MockOut.of(INTEGER);
        MockOut<Long> mlt = MockOut.of(BIGINT);
        when(jdbc.execute(call("test_math")
                .in("val1", 10)
                .in("val2", 20)
                .out("out_sum", sum)
                .out("out_mlt", mlt))).then(invocation -> {
            sum.setTo(invocation.getArguments()[0], 30);
            mlt.setTo(invocation.getArguments()[0], 200L);
            return null;
        });

        Result result = serviceCallNamed(jdbc, 10, 20);

        assertEquals(30, result.sum);
        assertEquals(200, result.mlt);
    }

    @Test
    public void testCallNamedFunctionMock() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.execute(call("get_concat", VARCHAR)
                .in("s2", "def")
                .in("s1", 4)))
                .thenReturn("4def");

        String result = serviceCallNamedFunction(jdbc, 4, "def");

        assertEquals("4def", result);
    }

    @Test
    public void testRefCursorOutParam() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        MockOut<List<Map.Entry<String, String>>> out = MockOut.of(cursor((rs, rowNum) -> {
            return immutableEntry("key", "value");
        }));
        when(jdbc.execute(call("proc_extras_tab")
                .in("extra_string", "1=value1;2=value2;6=value6;")
                .out("v_cur", out))).then(invocation -> {
            out.setTo(invocation.getArguments()[0], Arrays.asList(
                    immutableEntry("1", "value1"),
                    immutableEntry("2", "value2"),
                    immutableEntry("6", "value6")
            ));
            return null;
        });

        List<Map.Entry<String, String>> extras =
                serviceTestRefCursorOutParam(jdbc, "1=value1;2=value2;6=value6;");
        assertEquals(extras, Arrays.asList(
                immutableEntry("1", "value1"),
                immutableEntry("2", "value2"),
                immutableEntry("6", "value6")
        ));
    }

    private static List<Map.Entry<String, String>> serviceTestRefCursorOutParam(
            JdbcTemplate jdbc, String extra) {
        Out<List<Map.Entry<String, String>>> outExtras = Out.of(cursor((rs, rowNum) -> {
            return immutableEntry(rs.getString("id"), rs.getString("value"));
        }));
        jdbc.execute(call("proc_extras_tab")
                .in("extra_string", extra)
                .out("v_cur", outExtras));
        return outExtras.get();
    }

    private static Result serviceCallSql(JdbcTemplate jdbc, int val1, int val2) {
        Out<Integer> sum = Out.of(INTEGER);
        Out<Integer> mlt = Out.of(INTEGER);
        jdbc.execute(callSql("{call test_math(?, ?, ?, ?)}")
                .in(val1).in(val2).out(sum).out(mlt));
        return new Result(sum.get(), mlt.get());
    }

    private static Result serviceCallNamed(JdbcTemplate jdbc, int val1, int val2) {
        AtomicReference<Integer> sum = new AtomicReference<>();
        AtomicReference<Long> mlt = new AtomicReference<>();
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
