package org.morejdbc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.StatementCreatorUtils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.morejdbc.JdbcCall.callSql;
import static org.morejdbc.JdbcTemplateUtils.jdbc;
import static org.morejdbc.OracleSqlTypes.cursor;
import static org.morejdbc.SqlTypes.*;
import static org.morejdbc.TestUtils.immutableEntry;

/**
 * Follow instructions in readme-oracle-tests.md to prepare the database.
 */
public class OracleJdbcCallTest {

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
    public void testCall1() {
        Out<Integer> sum = Out.of(INTEGER);
        Out<Integer> mlt = Out.of(INTEGER);
        jdbc.execute(callSql("{call test_math(?, ?, ?, ?)}")
                .in(1).in(2).out(sum).out(mlt)
        );
        assertEquals(sum.get(), Integer.valueOf(3));
        assertEquals(mlt.get(), Integer.valueOf(2));
    }

    /**
     * the default datatype for null variables is defined as varchar2(32)
     */
    private static final SqlType<Object> UNKNOWN = SqlType.of("unknown", SqlTypeValue.TYPE_UNKNOWN,
            StatementCreatorUtils::setParameterValue, CallableStatement::getObject);

    @Test
    public void testCallBadArgs() {
        Out<Integer> sum = Out.of(INTEGER);
        Out<Integer> mlt = Out.of(INTEGER);
        try {
            jdbc.execute(callSql("{call test_math(?, ?, ?, ?, ?)}")
                    .in(1).in(2L).out(sum).out(mlt).in(null, UNKNOWN)
            );
            fail();
        } catch (BadSqlGrammarException e) {
            // sql via SqlProvider
            assertTrue(e.getMessage()
                    .contains("bad SQL grammar [{call test_math(?, ?, ?, ?, ?)}];"));
        }
    }

    @Test
    public void testInsertReturning() {
        // temp_pk_trigger (id number(9), value varchar2(20 char));
        // checks charset
        String valueIn = "тест" + System.currentTimeMillis();
        Out<Long> idOut = Out.of(BIGINT);
        AtomicReference<String> valueOut = new AtomicReference<>();
        jdbc.execute(callSql(
                "BEGIN INSERT INTO temp_pk_trigger(value) VALUES (?) " +
                        "RETURNING id, value INTO ?, ?; END;")
                .in(valueIn)
                .out(idOut).out(VARCHAR, valueOut::set)
        );
        System.out.println(idOut.get());
        System.out.println(valueOut.get());
        assertTrue(idOut.get() > 0);
        assertEquals(valueOut.get(), valueIn);
    }

    @Test
    public void testCallFuncResultSet() {
        Out<List<Map.Entry<String, String>>> extras = Out.of(cursor((row, rowNum) -> {
            return immutableEntry(row.getString("id"), row.getString("value"));
        }));
        jdbc.execute(callSql("{? = call get_extras_tab(?)}")
                .out(extras).in("1=value1;2=value2;6=value6;")
        );
        assertEquals(extras.get(), Arrays.asList(
                immutableEntry("1", "value1"),
                immutableEntry("2", "value2"),
                immutableEntry("6", "value6")
        ));
    }

    @Test
    public void testLongBlobConcat() {
        // > 4000 bytes
        byte[] blob1 = new byte[4096];
        byte[] blob2 = new byte[4096];
        ThreadLocalRandom.current().nextBytes(blob1);
        ThreadLocalRandom.current().nextBytes(blob2);

        Out<byte[]> result = Out.of(BLOB);

        jdbc.execute(callSql("{? = call blobs_concat(?, ?)}")
                .out(result)
                .in(blob1).in(blob2)
        );

        byte[] expected = TestUtils.concat(blob1, blob2);
        assertArrayEquals(expected, result.get());
    }
}
