package org.morejdbc;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

import java.sql.ResultSet;
import java.util.List;

/**
 * Oracle Extensions
 * https://docs.oracle.com/cd/E11882_01/java.112/e16548/oraint.htm#JJDBC28120
 */
public class OracleSqlTypes {

    /**
     * oracle.jdbc.OracleTypes.CURSOR = -10
     */
    private static final int ORACLE_TYPES_CURSOR = -10;

    /**
     * OracleTypes.CURSOR
     *
     * @param rowMapper
     * @param <T>
     * @return
     */
    public static <T> SqlType<List<T>> cursor(RowMapper<T> rowMapper) {
        return SqlType.of("cursor", ORACLE_TYPES_CURSOR, null, (cs, idx) -> {
            // Alternative way (Oracle documentation):
            // ResultSet rs = ((OracleCallableStatement) cs).getCursor(idx)
            // Also: cs.unwrap(OracleCallableStatement.class)
            try (ResultSet rs = (ResultSet) cs.getObject(idx)) {
                ResultSetExtractor<List<T>> extractor = new RowMapperResultSetExtractor<>(rowMapper);
                return extractor.extractData(rs);
            }
        });
    }

    private OracleSqlTypes() {
    }
}
