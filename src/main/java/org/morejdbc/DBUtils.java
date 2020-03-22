package org.morejdbc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.*;

public class DBUtils {

    @Nullable
    public static Integer getIntOrNull(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    @Nullable
    public static Integer getIntOrNull(CallableStatement cs, int idx) throws SQLException {
        int value = cs.getInt(idx);
        if (cs.wasNull()) {
            return null;
        }
        return value;
    }

    @Nullable
    public static Long getLongOrNull(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    @Nullable
    public static Long getLongOrNull(CallableStatement cs, int idx) throws SQLException {
        long value = cs.getLong(idx);
        if (cs.wasNull()) {
            return null;
        }
        return value;
    }

    @Nullable
    public static Double getDoubleOrNull(ResultSet rs, String columnName) throws SQLException {
        double value = rs.getDouble(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    @Nullable
    public static byte[] getBlobBytes(ResultSet rs, String columnName) throws SQLException {
        Blob blob = rs.getBlob(columnName);
        try {
            return blob == null || blob.length() == 0 ? null : blob.getBytes(1, (int) blob.length());
        } finally {
            if (blob != null) {
                blob.free();
            }
        }
    }

    @Nullable
    public static byte[] getBlobBytes(CallableStatement cs, int idx) throws SQLException {
        Blob blob = cs.getBlob(idx);
        try {
            return blob == null || blob.length() == 0 ? null : blob.getBytes(1, (int) blob.length());
        } finally {
            if (blob != null) {
                blob.free();
            }
        }
    }

    private DBUtils() {
    }
}
