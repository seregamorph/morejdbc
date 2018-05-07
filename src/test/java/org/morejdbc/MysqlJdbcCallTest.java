package org.morejdbc;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.morejdbc.JdbcCall.callSql;
import static org.morejdbc.SqlTypes.INTEGER;

public class MysqlJdbcCallTest {

    private Connection connection;
    private JdbcTemplate jdbc;

    @Before
    public void before() throws SQLException {
        Properties props = TestUtils.propertiesFromString(TestUtils.readString("mysql_test.properties"));
        this.connection = DriverManager.getConnection(props.getProperty("url"), props);
        DataSource dataSource = JdbcTemplateUtils.smartDataSource(this.connection);
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @After
    public void after() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void testMath() {
        Out<Integer> sum = Out.of(INTEGER);
        Out<Integer> mlt = Out.of(INTEGER);
        jdbc.execute(callSql("{call test_math(?, ?, ?, ?)}")
                .in(10).in(20).out(sum).out(mlt)
        );
        assertEquals(30, sum.get().intValue());
        assertEquals(200, mlt.get().intValue());
    }
}
