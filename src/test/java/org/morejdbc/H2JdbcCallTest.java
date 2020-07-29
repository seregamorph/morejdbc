package org.morejdbc;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.morejdbc.JdbcCall.callSql;
import static org.morejdbc.SqlTypes.INTEGER;

public class H2JdbcCallTest {

    private Connection connection;

    private JdbcTemplate jdbc;

    @BeforeEach
    public void before() throws SQLException {
        Properties props = TestUtils.propertiesFromString(TestUtils.readString("h2_test.properties"));
        this.connection = DriverManager.getConnection(props.getProperty("url"), props);
        DataSource dataSource = TestUtils.smartDataSource(this.connection);
        this.jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE ALIAS mult FOR \"org.morejdbc.H2Functions.mult\"");
    }

    @AfterEach
    public void after() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void testPureJdbc() throws SQLException {
        CallableStatement call = connection.prepareCall("{? = call mult(?, ?)}");
        call.registerOutParameter(1, Types.INTEGER);
        call.setInt(2, 2);
        call.setInt(3, 3);
        call.execute();
        assertEquals(6, call.getInt(1));
    }

    @Test
    public void testMultOut() {
        Out<Integer> out = Out.of(INTEGER);

        jdbc.execute(callSql("{? = call mult(?, ?)}")
                .out(out)
                .in(2)
                .in(3));

        assertEquals(Integer.valueOf(6), out.get());
    }

    @Test
    public void testMultConsumer() {
        AtomicInteger out = new AtomicInteger();

        jdbc.execute(callSql("{? = call mult(?, ?)}")
                .out(INTEGER, out::set)
                .in(2)
                .in(3));

        assertEquals(6, out.get());
    }
}
