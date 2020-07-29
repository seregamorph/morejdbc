package org.morejdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.morejdbc.JdbcCall.callSql;
import static org.morejdbc.OracleSqlTypes.cursor;
import static org.morejdbc.SqlTypes.*;
import static org.morejdbc.TestUtils.immutableEntry;
import static org.morejdbc.TestUtils.jdbc;

/**
 * Follow instructions in readme-oracle-tests.md to prepare the database.
 */
public class OracleJdbcCallTest {

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
    public void testCall1() {
        Out<Integer> sum = Out.of(INTEGER);
        Out<Integer> mlt = Out.of(INTEGER);

        jdbc.execute(callSql("{call test_more_jdbc_pkg.calc_sum_and_multiply_of_two_numbers(?, ?, ?, ?)}")
                .in(8)
                .in(3)
                .out(sum)
                .out(mlt));

        assertEquals(Integer.valueOf(11), sum.get());
        assertEquals(Integer.valueOf(24), mlt.get());
    }

    @Test
    public void testCallBadArgs() {
        Out<Integer> sum = Out.of(INTEGER);
        Out<Integer> mlt = Out.of(INTEGER);
        try {
            jdbc.execute(callSql("{call test_more_jdbc_pkg.calc_sum_and_multiply_of_two_numbers(?, ?, ?, ?, ?)}")
                    .in(0)
                    .in(1L)
                    .out(sum)
                    .out(mlt)
                    .in(null, UNKNOWN));

            fail();
        } catch (BadSqlGrammarException e) {
            // sql via SqlProvider
            assertTrue(e.getMessage().contains("bad SQL grammar [{call test_more_jdbc_pkg.calc_sum_and_multiply_of_two_numbers(?, ?, ?, ?, ?)}];"));
        }
    }

    @Test
    public void testInsertReturning() {
        // table_with_identity_pk (id number(9) identity primary key, value varchar2(20 char));
        // checks charset
        String valueIn = "тест" + System.currentTimeMillis();
        Out<Long> idOut = Out.of(BIGINT);
        AtomicReference<String> valueOut = new AtomicReference<>();

        jdbc.execute(callSql("begin insert into table_with_identity_pk (value) values (?) " + "returning id, value into ?, ?; end;")
                .in(valueIn)
                .out(idOut)
                .out(VARCHAR, valueOut::set));

        System.out.println(idOut.get());
        System.out.println(valueOut.get());
        assertTrue(idOut.get() > 0);
        assertEquals(valueIn, valueOut.get());
    }

    @Test
    public void testCallFuncResultSet() {
        Out<List<Map.Entry<String, String>>> extras = Out.of(cursor((row, rowNum) -> immutableEntry(row.getString("id"), row.getString("value"))));

        jdbc.execute(callSql("{? = call test_more_jdbc_pkg.get_cursor_from_key_value_as_string(?)}")
                .out(extras)
                .in("1=value1;2=value2;6=value6;"));

        assertEquals(Arrays.asList(immutableEntry("1", "value1"), immutableEntry("2", "value2"), immutableEntry("6", "value6")), extras.get());
    }

    @Test
    public void testLongBlobConcat() {
        // > 4000 bytes
        byte[] blob1 = new byte[4096];
        byte[] blob2 = new byte[4096];
        ThreadLocalRandom.current().nextBytes(blob1);
        ThreadLocalRandom.current().nextBytes(blob2);
        Out<byte[]> result = Out.of(BLOB);

        jdbc.execute(callSql("{? = call test_more_jdbc_pkg.get_two_blobs_concatenated(?, ?)}")
                .out(result)
                .in(blob1)
                .in(blob2));

        byte[] expected = TestUtils.concat(blob1, blob2);
        assertArrayEquals(expected, result.get());
    }
}
