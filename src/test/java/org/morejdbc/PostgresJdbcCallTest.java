package org.morejdbc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.morejdbc.JdbcCall.callSql;
import static org.morejdbc.JdbcTemplateUtils.jdbc;
import static org.morejdbc.SqlTypes.NUMERIC;

public class PostgresJdbcCallTest {

    private Connection connection;
    private JdbcTemplate jdbc;

    @Before
    public void before() throws SQLException {
        Properties props = TestUtils.propertiesFromString(TestUtils.readString("psqltest.properties"));
        this.connection = DriverManager.getConnection(props.getProperty("url"), props);
        this.jdbc = jdbc(this.connection);
    }

    @After
    public void after() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void test() {
        List<Integer> list = jdbc.query("SELECT id FROM test", (row, rowNum) -> row.getInt(1));
        System.out.println(list);
    }

    @Test
    public void testCallExecute() {
        jdbc.execute(con -> {
            CallableStatement cs = con.prepareCall("{ call hi_lo( ?, ?, ?, ?, ? ) }");
            cs.setInt(1, 10);
            cs.setInt(2, 20);
            cs.setInt(3, 30);
            cs.registerOutParameter(4, SqlTypes.NUMERIC.getSqlType());
            cs.registerOutParameter(5, SqlTypes.NUMERIC.getSqlType());
            return cs;
        }, (CallableStatementCallback<Object>) cs -> {
            cs.execute();

            assertEquals(new BigDecimal(30), cs.getBigDecimal(4));
            assertEquals(new BigDecimal(10), cs.getBigDecimal(5));
            return null;
        });
    }

    @Test
    public void testCall() {
        jdbc.execute(callSql(
                "{call hi_lo(?, ?, ?, ?, ?)}")
                .in(10)
                .in(20)
                .in(30)
                .out(NUMERIC, hi -> System.out.println("hi " + hi))
                .out(NUMERIC, lo -> System.out.println("lo " + lo))
        );
    }
}
