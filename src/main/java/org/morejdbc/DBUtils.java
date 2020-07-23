package org.morejdbc;

import org.jetbrains.annotations.Nullable;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtils {

    @Nullable
    public static Integer getIntOrNull(ResultSet rs, String columnName) throws SQLException {
        var value = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    @Nullable
    public static Integer getIntOrNull(CallableStatement cs, int idx) throws SQLException {
        var value = cs.getInt(idx);
        if (cs.wasNull()) {
            return null;
        }
        return value;
    }

    @Nullable
    public static Long getLongOrNull(ResultSet rs, String columnName) throws SQLException {
        var value = rs.getLong(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    @Nullable
    public static Long getLongOrNull(CallableStatement cs, int idx) throws SQLException {
        var value = cs.getLong(idx);
        if (cs.wasNull()) {
            return null;
        }
        return value;
    }

    @Nullable
    public static Double getDoubleOrNull(ResultSet rs, String columnName) throws SQLException {
        var value = rs.getDouble(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    @Nullable
    public static byte[] getBlobBytes(ResultSet rs, String columnName) throws SQLException {
        var blob = rs.getBlob(columnName);
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
        var blob = cs.getBlob(idx);
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
