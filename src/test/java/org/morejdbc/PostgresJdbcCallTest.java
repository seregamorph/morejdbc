package org.morejdbc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.morejdbc.JdbcCall.callSql;
import static org.morejdbc.PostgresSqlTypes.cursor;
import static org.morejdbc.SqlTypes.NUMERIC;

public class PostgresJdbcCallTest {

    private Connection connection;
    private JdbcTemplate jdbc;
    private TransactionTemplate transactionTemplate;

    @Before
    public void before() throws SQLException {
        Properties props = TestUtils.propertiesFromString(TestUtils.readString("psql_test.properties"));
        this.connection = DriverManager.getConnection(props.getProperty("url"), props);
        DataSource dataSource = JdbcTemplateUtils.smartDataSource(this.connection);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @After
    public void after() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void testSelect() {
        List<HiLo> list = jdbc.query(
                "SELECT hi, lo FROM hi_lo(?, ?, ?)",
                (row, rowNum) -> {
                    HiLo hl = new HiLo();
                    hl.hi = row.getInt("hi");
                    hl.lo = row.getInt("lo");
                    return hl;
                },
                10, 20, 30
        );
        assertEquals(1, list.size());
        HiLo hl = list.get(0);
        assertEquals(30, hl.hi);
        assertEquals(10, hl.lo);
    }

    @Test
    public void testCallExecute() {
        jdbc.execute(con -> {
            CallableStatement cs = con.prepareCall("{call hi_lo(?, ?, ?, ?, ?)}");
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
        AtomicReference<BigDecimal> hi = new AtomicReference<>();
        AtomicReference<BigDecimal> lo = new AtomicReference<>();
        jdbc.execute(callSql(
                "{call hi_lo(?, ?, ?, ?, ?)}")
                .in(10)
                .in(20)
                .in(30)
                .out(NUMERIC, hi::set)
                .out(NUMERIC, lo::set)
        );
        assertEquals(30, hi.get().intValue());
        assertEquals(10, lo.get().intValue());
    }

    @Test
    public void testRefcursor() {
        // refcursor out works only in transaction
        List<Integer> values = transactionTemplate.execute(transaction -> {
            Out<List<Integer>> outValues = Out.of(cursor((row, rowNum) -> row.getInt(1)));
            jdbc.execute(callSql("{ ? = call refcursorfunc() }")
                    .out(outValues)
            );
            return outValues.get();
        });
        assertEquals(Arrays.asList(1, 2), values);
    }

    private static class HiLo {
        int hi;
        int lo;
    }
}
