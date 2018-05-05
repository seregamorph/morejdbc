package org.morejdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.SmartDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.util.Optional;

public class JdbcTemplateUtils {

    public static <T> ResultSetExtractor<T> singleOrNull(RowMapper<T> rowMapper) {
        return rs -> {
            if (!rs.next()) {
                return null;
            }
            T value = rowMapper.mapRow(rs, 0);
            if (rs.next()) {
                throw new SQLConstraintException("Unexpected more than one row in ResultSet. First " + value);
            }
            return value;
        };
    }

    public static <T> ResultSetExtractor<Optional<T>> singleOrEmpty(RowMapper<T> rowMapper) {
        return rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }
            T value = rowMapper.mapRow(rs, 0);
            if (rs.next()) {
                throw new SQLConstraintException("Unexpected more than one row in ResultSet. First " + value);
            }
            return Optional.of(value);
        };
    }

    /**
     * Fail if currentThread is not inside of spring Transaction
     */
    public static void assertTransaction() {
        Assert.state(TransactionSynchronizationManager.isActualTransactionActive(), "Transaction is not started");
    }

    public static NamedParameterJdbcTemplate namedJdbc(@Nonnull Connection connection) {
        JdbcTemplate jdbc = jdbc(connection);
        return new NamedParameterJdbcTemplate(jdbc);
    }

    public static JdbcTemplate jdbc(@Nonnull Connection connection) {
        SmartDataSource ds = smartDataSource(connection);
        return new JdbcTemplate(ds);
    }

    private static SmartDataSource smartDataSource(Connection connection) {
        return new SingleConnectionDataSource(connection, false);
    }

    private JdbcTemplateUtils() {
    }
}
