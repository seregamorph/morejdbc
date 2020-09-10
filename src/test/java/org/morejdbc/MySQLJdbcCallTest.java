package org.morejdbc;

import static org.junit.Assert.assertEquals;
import static org.morejdbc.JdbcCall.callSql;
import static org.morejdbc.SqlTypes.INTEGER;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MySQLContainer;

public class MySQLJdbcCallTest {

    @ClassRule
    public static final MySQLContainer<?> mysql = new MySQLContainer<>()
            .withInitScript("sql/mysql/schema.sql");

    private Connection connection;
    private JdbcTemplate jdbc;

    @Before
    public void before() throws SQLException {
        connection = DriverManager.getConnection(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
        DataSource dataSource = TestUtils.smartDataSource(this.connection);
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
