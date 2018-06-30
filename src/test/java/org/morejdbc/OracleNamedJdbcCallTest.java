package org.morejdbc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.morejdbc.JdbcTemplateUtils.jdbc;
import static org.morejdbc.NamedJdbcCall.call;
import static org.morejdbc.OracleSqlTypes.cursor;
import static org.morejdbc.SqlTypes.*;
import static org.morejdbc.TestUtils.immutableEntry;

public class OracleNamedJdbcCallTest {

    private Connection connection;
    private JdbcTemplate jdbc;

    @Before
    public void before() throws SQLException {
        Properties props = TestUtils.propertiesFromString(TestUtils.readString("oracle_test.properties"));
        Locale def = Locale.getDefault();
        try {
            // workarond for XE with russian locale
            Locale.setDefault(Locale.ENGLISH);
            this.connection = DriverManager.getConnection(props.getProperty("url"), props);
        } finally {
            Locale.setDefault(def);
        }
        this.jdbc = jdbc(this.connection);
    }

    @After
    public void after() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void testNamedCall1() {
        Out<Integer> sum = Out.of(INTEGER);
        Out<Long> mlt = Out.of(BIGINT);
        jdbc.execute(call("test_math")
                .in("val1", 1)
                .in("val2", 2L)
                .out("out_sum", sum)
                .out("out_mlt", mlt)
        );
        assertEquals(sum.get(), Integer.valueOf(3));
        assertEquals(mlt.get(), Long.valueOf(2L));
    }

    @Test
    public void testNamedCall1Consumer() {
        AtomicReference<Integer> sum = new AtomicReference<>();
        AtomicReference<Long> mlt = new AtomicReference<>();
        jdbc.execute(call("test_math")
                .in("val1", 1)
                .in("val2", 2L)
                .out("out_sum", INTEGER, sum::set)
                .out("out_mlt", BIGINT, mlt::set)
        );
        assertEquals(sum.get(), Integer.valueOf(3));
        assertEquals(mlt.get(), Long.valueOf(2L));
    }

    @Test
    public void testNamedCall2() {
        Out<Integer> sum = Out.of(INTEGER);
        Out<Integer> mlt = Out.of(INTEGER);
        jdbc.execute(call("test_math")
                .out("out_mlt", mlt)
                .out("out_sum", sum)
                .in("val1", "1")
                .in("val2", new BigDecimal(2))
        );

        assertEquals(sum.get(), Integer.valueOf(3));
        assertEquals(mlt.get(), Integer.valueOf(2));
    }

    @Test
    public void testNamedCall3() {
        NamedJdbcCall<Void> call = call("test_math")
                .in("val1", 1)
                .in("val2", 2);
        Out<Integer> sum = call.out("out_sum", INTEGER);
        Out<Integer> mlt = call.out("out_mlt", INTEGER);
        jdbc.execute(call);

        assertEquals(sum.get(), Integer.valueOf(3));
        assertEquals(mlt.get(), Integer.valueOf(2));
    }

    @Test
    public void testNamedCallFunc1() {
        String result = jdbc.execute(call("get_concat", VARCHAR)
                .in("s1", "abc")
                .in("s2", (String) null)
        );
        assertEquals(result, "abc");
    }

    @Test
    public void testNamedCallFunc2() {
        // reorder s1, s2
        String result = jdbc.execute(call("get_concat", VARCHAR)
                .in("s2", new StringBuilder("WL-1"))
                .in("s1", "abc")
        );
        assertEquals(result, "abcWL-1");
    }

    @Test
    public void testNamedCallFunc3() {
        String result = jdbc.execute(call("get_concat", VARCHAR)
                .in("s2", "def")
                .in("s1", 4)
        );
        assertEquals(result, "4def");
    }

    /**
     * the default datatype for null variables is defined as varchar2(32)
     */
    private static final SqlType<Object> UNKNOWN = SqlType.of("unknown", SqlTypeValue.TYPE_UNKNOWN,
            StatementCreatorUtils::setParameterValue, CallableStatement::getObject);

    @Test
    public void testNamedCallFunc4() {
        // pass null-string with unknown type
        String result = jdbc.execute(call("get_concat", VARCHAR)
                .in("s1", "abc")
                .in("s2", null, UNKNOWN)
        );
        assertEquals(result, "abc");
    }

    @Test
    public void testNamedCallFuncResultSet() {
        RowMapper<Map.Entry<String, String>> mapper = (rs, rowNum) -> {
            return immutableEntry(rs.getString("id"), rs.getString("value"));
        };
        List<Map.Entry<String, String>> extras = jdbc.execute(call("get_extras_tab", cursor(mapper))
                .in("extra_string", "1=value1;2=value2;6=value6;")
        );
        assertEquals(extras, Arrays.asList(
                immutableEntry("1", "value1"),
                immutableEntry("2", "value2"),
                immutableEntry("6", "value6")
        ));
    }

    @Test
    public void testNamedCallInOut1() {
        Out<BigDecimal> sum = Out.of(DECIMAL);
        jdbc.execute(call("test_in_out")
                .in("x", 1)
                .inOut("io_sum", new BigDecimal(5), sum)
                .in("y", 2)
        );

        assertEquals(sum.get(), new BigDecimal(8));
    }

    @Test
    public void testNamedCallInOut2() {
        Out<Integer> sum = Out.of(INTEGER);
        jdbc.execute(call("test_in_out")
                .in("x", 1)
                .inOut("io_sum", 5, sum)
                .in("y", 2)
        );

        assertEquals(sum.get(), Integer.valueOf(8));
    }

    @Test
    public void testNamedCallInOut2Consumer() {
        AtomicReference<Integer> sum = new AtomicReference<>();
        jdbc.execute(call("test_in_out")
                .in("x", 1)
                .inOut("io_sum", 5, sum::set)
                .in("y", 2)
        );

        assertEquals(sum.get(), Integer.valueOf(8));
    }

    @Test
    public void testPackageCallable4arg() {
        Out<Long> sum = Out.of(BIGINT);
        Out<Integer> mlt = Out.of(INTEGER);
        jdbc.execute(call("test_math")
                .out("out_sum", sum)
                .in("val1", 1)
                .in("val2", 2)
                .out("out_mlt", mlt)
        );
        assertEquals(sum.get(), Long.valueOf(3L));
        assertEquals(mlt.get(), Integer.valueOf(2));
    }

    @Test
    public void testPackageCallable3arg() {
        Out<Integer> sum = Out.of(INTEGER);
        jdbc.execute(call("test_math")
                .in("val1", 1)
                .in("val2", 2)
                .out("out_sum", sum)
                .out("out_mlt", Out.of(INTEGER))
        );
        assertEquals(sum.get(), Integer.valueOf(3));
    }

    @Test
    public void testLongBinaryConcat() {
        // > 4000 bytes
        byte[] blob1 = new byte[4096];
        byte[] blob2 = new byte[4096];
        ThreadLocalRandom.current().nextBytes(blob1);
        ThreadLocalRandom.current().nextBytes(blob2);

        byte[] result = jdbc.execute(call("blobs_concat", BINARY)
                .in("b1", blob1, BINARY)
                .in("b2", blob2, BINARY)
        );

        byte[] expected = TestUtils.concat(blob1, blob2);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testLongBlobConcat() {
        // > 4000 bytes
        byte[] blob1 = new byte[4096];
        byte[] blob2 = new byte[4096];
        ThreadLocalRandom.current().nextBytes(blob1);
        ThreadLocalRandom.current().nextBytes(blob2);

        byte[] result = jdbc.execute(call("blobs_concat", BLOB)
                .in("b1", blob1)
                .in("b2", blob2)
        );

        byte[] expected = TestUtils.concat(blob1, blob2);
        assertArrayEquals(expected, result);
    }
}
