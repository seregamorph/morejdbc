package org.morejdbc;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.List;

/**
 * http://www.sqlines.com/postgresql/how-to/return_result_set_from_stored_procedure
 */
public class PostgresSqlTypes {

    public static <T> SqlType<List<T>> cursor(RowMapper<T> rowMapper) {
        return SqlType.of("cursor", Types.OTHER, null, (cs, idx) -> {
            try (ResultSet rs = (ResultSet) cs.getObject(idx)) {
                ResultSetExtractor<List<T>> extractor = new RowMapperResultSetExtractor<>(rowMapper);
                return extractor.extractData(rs);
            }
        });
    }

    private PostgresSqlTypes() {
    }
}
