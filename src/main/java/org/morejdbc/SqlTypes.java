package org.morejdbc;

import org.springframework.jdbc.core.StatementCreatorUtils;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * Type bindings
 * http://ladyj.eu/oracle/default-mappings-between-sql-types-and-java-types
 */
public class SqlTypes {

    /**
     * Types.INTEGER
     */
    public static final SqlType<Integer> INTEGER = SqlType.of("integer", Types.INTEGER,
            StatementCreatorUtils::setParameterValue, DBUtils::getIntOrNull);

    /**
     * Types.BIGINT
     */
    public static final SqlType<Long> BIGINT = SqlType.of("bigint", Types.BIGINT,
            StatementCreatorUtils::setParameterValue, DBUtils::getLongOrNull);

    /**
     * Types.NUMERIC
     */
    public static final SqlType<BigDecimal> NUMERIC = SqlType.of("numeric", Types.NUMERIC,
            StatementCreatorUtils::setParameterValue, CallableStatement::getBigDecimal);

    /**
     * Types.DECIMAL
     */
    public static final SqlType<BigDecimal> DECIMAL = SqlType.of("decimal", Types.DECIMAL,
            StatementCreatorUtils::setParameterValue, CallableStatement::getBigDecimal);

    /**
     * Types.VARCHAR
     */
    public static final SqlType<String> VARCHAR = SqlType.of("varchar", Types.VARCHAR,
            StatementCreatorUtils::setParameterValue, CallableStatement::getString);

    /**
     * Types.BINARY
     */
    public static final SqlType<byte[]> BINARY = SqlType.of("binary", Types.BINARY,
            StatementCreatorUtils::setParameterValue, CallableStatement::getBytes);

    /**
     * Types.BLOB
     */
    public static final SqlType<byte[]> BLOB = SqlType.of("blob", Types.BLOB,
            (cs, idx, sqlType, bytes) -> {
                // Oracle does not accept BLOB size 0 bytes (unlike SqlTypes.BINARY), pass as null
                if (bytes == null || bytes.length == 0) {
                    cs.setBlob(idx, (Blob) null);
                } else {
                    cs.setBlob(idx, new ByteArrayInputStream(bytes), bytes.length);
                }
            }, DBUtils::getBlobBytes);

    /**
     * Types.TIMESTAMP
     */
    public static final SqlType<Timestamp> TIMESTAMP = SqlType.of("timestamp", Types.TIMESTAMP,
            StatementCreatorUtils::setParameterValue, CallableStatement::getTimestamp);

    private SqlTypes() {
    }
}
