package org.morejdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.morejdbc.NamedJdbcCall.call;
import static org.morejdbc.OracleSqlTypes.cursor;
import static org.morejdbc.SqlTypes.*;
import static org.morejdbc.TestUtils.immutableEntry;
import static org.morejdbc.TestUtils.jdbc;

/**
 * Follow instructions in readme-oracle-tests.md to prepare the database.
 */
public class OracleNamedJdbcCallTest {

    /**
     * the default datatype for null variables is defined as varchar2(32)
     */
    private static final SqlType<Object> UNKNOWN = SqlType.of("unknown", SqlTypeValue.TYPE_UNKNOWN, StatementCreatorUtils::setParameterValue, CallableStatement::getObject);

    private Connection connection;

    private JdbcTemplate jdbc;

    @BeforeEach
    public void before() throws SQLException {
        Properties props = TestUtils.propertiesFromString(TestUtils.readString("oracle_test.properties"));
        Locale def = Locale.getDefault();
        try {
            // workaround for XE with russian locale
            Locale.setDefault(Locale.ENGLISH);
            this.connection = DriverManager.getConnection(props.getProperty("url"), props);
        } finally {
            Locale.setDefault(def);
        }
        this.jdbc = jdbc(this.connection);
    }

    @AfterEach
    public void after() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void testNamedCall1() {
        Out<Integer> sum = Out.of(INTEGER);
        Out<Long> mlt = Out.of(BIGINT);

        jdbc.execute(call("test_more_jdbc_pkg.calc_sum_and_multiply_of_two_numbers")
                .in("p_number1", 1)
                .in("p_number2", 2L)
                .out("po_sum", sum)
                .out("po_mlt", mlt));

        assertEquals(Integer.valueOf(3), sum.get());
        assertEquals(Long.valueOf(2L), mlt.get());
    }

    @Test
    public void testNamedCall1Consumer() {
        AtomicReference<Integer> sum = new AtomicReference<>();
        AtomicReference<Long> mlt = new AtomicReference<>();

        jdbc.execute(call("test_more_jdbc_pkg.calc_sum_and_multiply_of_two_numbers")
                .in("p_number1", 1)
                .in("p_number2", 2L)
                .out("po_sum", INTEGER, sum::set)
                .out("po_mlt", BIGINT, mlt::set));

        assertEquals(Integer.valueOf(3), sum.get());
        assertEquals(Long.valueOf(2L), mlt.get());
    }

    @Test
    public void testNamedCall2() {
        Out<Integer> sum = Out.of(INTEGER);
        Out<Integer> mlt = Out.of(INTEGER);

        jdbc.execute(call("test_more_jdbc_pkg.calc_sum_and_multiply_of_two_numbers")
                .out("po_mlt", mlt)
                .out("po_sum", sum)
                .in("p_number1", "1")
                .in("p_number2", new BigDecimal(2)));

        assertEquals(Integer.valueOf(3), sum.get());
        assertEquals(Integer.valueOf(2), mlt.get());
    }

    @Test
    public void testNamedCall3() {
        NamedJdbcCall<Void> call = call("test_more_jdbc_pkg.calc_sum_and_multiply_of_two_numbers")
                .in("p_number1", 1)
                .in("p_number2", 2);
        Out<Integer> sum = call.out("po_sum", INTEGER);
        Out<Integer> mlt = call.out("po_mlt", INTEGER);
        jdbc.execute(call);

        assertEquals(Integer.valueOf(3), sum.get());
        assertEquals(Integer.valueOf(2), mlt.get());
    }

    @Test
    public void testNamedCallFunc1() {
        String result = jdbc.execute(call("test_more_jdbc_pkg.get_concat_of_two_strings", VARCHAR)
                .in("p_string1", "abc")
                .in("p_string2", (String) null));

        assertEquals("abc", result);
    }

    @Test
    public void testNamedCallFunc2() {
        // reordered p_string1, p_string2
        String result = jdbc.execute(call("test_more_jdbc_pkg.get_concat_of_two_strings", VARCHAR)
                .in("p_string2", new StringBuilder("XYZ"))
                .in("p_string1", "abc"));

        assertEquals("abcXYZ", result);
    }

    @Test
    public void testNamedCallFunc3() {
        String result = jdbc.execute(call("test_more_jdbc_pkg.get_concat_of_two_strings", VARCHAR)
                .in("p_string2", "def")
                .in("p_string1", 4));

        assertEquals("4def", result);
    }

    @Test
    public void testNamedCallFunc4() {
        // pass null-string with unknown type
        String result = jdbc.execute(call("test_more_jdbc_pkg.get_concat_of_two_strings", VARCHAR)
                .in("p_string1", "abc")
                .in("p_string2", null, UNKNOWN));

        assertEquals("abc", result);
    }

    @Test
    public void testNamedCallFuncResultSet() {
        RowMapper<Map.Entry<String, String>> mapper = (rs, rowNum) -> immutableEntry(rs.getString("id"), rs.getString("value"));

        List<Map.Entry<String, String>> extras = jdbc.execute(call("test_more_jdbc_pkg.get_cursor_from_key_value_as_string", cursor(mapper))
                .in("p_key_value_string", "1=value1;2=value2;6=value6;"));

        assertEquals(Arrays.asList(immutableEntry("1", "value1"), immutableEntry("2", "value2"), immutableEntry("6", "value6")), extras);
    }

    @Test
    public void testRefCursorOutParam() {
        RowMapper<Map.Entry<String, String>> mapper = (rs, rowNum) -> immutableEntry(rs.getString("id"), rs.getString("value"));
        Out<List<Map.Entry<String, String>>> outExtras = Out.of(cursor(mapper));

        jdbc.execute(call("test_more_jdbc_pkg.get_cursor_from_key_value_as_string")
                .in("p_key_value_string", "1=value1;2=value2;6=value6;")
                .out("po_cursor", outExtras));

        assertEquals(Arrays.asList(immutableEntry("1", "value1"), immutableEntry("2", "value2"), immutableEntry("6", "value6")), outExtras.get());
    }

    @Test
    public void testNamedCallInOut1() {
        Out<BigDecimal> sum = Out.of(DECIMAL);

        jdbc.execute(call("test_more_jdbc_pkg.calc_sum_of_two_numbers_with_in_out_parameter")
                .in("p_number1", 1)
                .inOut("pio_sum", new BigDecimal(5), sum)
                .in("p_number2", 2));

        assertEquals(new BigDecimal(8), sum.get());
    }

    @Test
    public void testNamedCallInOut2() {
        Out<Integer> sum = Out.of(INTEGER);

        jdbc.execute(call("test_more_jdbc_pkg.calc_sum_of_two_numbers_with_in_out_parameter")
                .in("p_number1", 1)
                .inOut("pio_sum", 5, sum)
                .in("p_number2", 2));

        assertEquals(Integer.valueOf(8), sum.get());
    }

    @Test
    public void testNamedCallInOut2Consumer() {
        AtomicReference<Integer> sum = new AtomicReference<>();

        jdbc.execute(call("test_more_jdbc_pkg.calc_sum_of_two_numbers_with_in_out_parameter")
                .in("p_number1", 1)
                .inOut("pio_sum", 5, sum::set)
                .in("p_number2", 2));

        assertEquals(Integer.valueOf(8), sum.get());
    }

    @Test
    public void testPackageCallable4arg() {
        Out<Long> sum = Out.of(BIGINT);
        Out<Integer> mlt = Out.of(INTEGER);

        jdbc.execute(call("test_more_jdbc_pkg.calc_sum_and_multiply_of_two_numbers")
                .out("po_sum", sum)
                .in("p_number1", 1)
                .in("p_number2", 2)
                .out("po_mlt", mlt));

        assertEquals(Long.valueOf(3L), sum.get());
        assertEquals(Integer.valueOf(2), mlt.get());
    }

    @Test
    public void testPackageCallable3arg() {
        Out<Integer> sum = Out.of(INTEGER);

        jdbc.execute(call("test_more_jdbc_pkg.calc_sum_and_multiply_of_two_numbers")
                .in("p_number1", 1)
                .in("p_number2", 2)
                .out("po_sum", sum)
                .out("po_mlt", Out.of(INTEGER)));

        assertEquals(Integer.valueOf(3), sum.get());
    }

    @Test
    public void testLongBinaryConcat() {
        // > 4000 bytes
        byte[] blob1 = new byte[4096];
        byte[] blob2 = new byte[4096];
        ThreadLocalRandom.current().nextBytes(blob1);
        ThreadLocalRandom.current().nextBytes(blob2);

        byte[] result = jdbc.execute(call("test_more_jdbc_pkg.get_two_blobs_concatenated", BINARY)
                .in("p_blob1", blob1, BINARY)
                .in("p_blob2", blob2, BINARY));

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

        byte[] result = jdbc.execute(call("test_more_jdbc_pkg.get_two_blobs_concatenated", BLOB)
                .in("p_blob1", blob1)
                .in("p_blob2", blob2));

        byte[] expected = TestUtils.concat(blob1, blob2);
        assertArrayEquals(expected, result);
    }
}
