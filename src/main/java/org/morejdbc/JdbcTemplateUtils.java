package org.morejdbc;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

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

    private JdbcTemplateUtils() {
    }
}
