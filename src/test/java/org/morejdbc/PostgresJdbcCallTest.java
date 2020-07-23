package org.morejdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.morejdbc.JdbcCall.callSql;
import static org.morejdbc.PostgresSqlTypes.cursor;
import static org.morejdbc.SqlTypes.NUMERIC;

/**
 * Follow instructions in readme-postgres-tests.md to prepare the database.
 */
public class PostgresJdbcCallTest {

    private Connection connection;

    private JdbcTemplate jdbc;

    private TransactionTemplate transactionTemplate;

    @BeforeEach
    public void before() throws SQLException {
        var props = TestUtils.propertiesFromString(TestUtils.readString("psql_test.properties"));
        this.connection = DriverManager.getConnection(props.getProperty("url"), props);
        DataSource dataSource = TestUtils.smartDataSource(this.connection);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @AfterEach
    public void after() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void testSelect() {
        var list = jdbc.query("SELECT hi, lo FROM hi_lo(?, ?, ?)", (row, rowNum) -> {
            var hl = new HiLo();
            hl.hi = row.getInt("hi");
            hl.lo = row.getInt("lo");
            return hl;
        }, 10, 20, 30);
        assertEquals(1, list.size());
        var hl = list.get(0);
        assertEquals(30, hl.hi);
        assertEquals(10, hl.lo);
    }

    @Test
    public void testCallExecute() {
        jdbc.execute(con -> {
            var cs = con.prepareCall("{call hi_lo(?, ?, ?, ?, ?)}");
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
        var hi = new AtomicReference<BigDecimal>();
        var lo = new AtomicReference<BigDecimal>();

        jdbc.execute(callSql("{call hi_lo(?, ?, ?, ?, ?)}")
                .in(10)
                .in(20)
                .in(30)
                .out(NUMERIC, hi::set)
                .out(NUMERIC, lo::set));

        assertEquals(30, hi.get().intValue());
        assertEquals(10, lo.get().intValue());
    }

    @Test
    public void testRefcursor() {
        // refcursor out works only in transaction
        var values = transactionTemplate.execute(transaction -> {
            var outValues = Out.of(cursor((row, rowNum) -> row.getInt(1)));

            jdbc.execute(callSql("{ ? = call refcursorfunc() }").out(outValues));

            return outValues.get();
        });
        assertEquals(Arrays.asList(1, 2), values);
    }

    private static class HiLo {
        int hi;
        int lo;
    }
}
