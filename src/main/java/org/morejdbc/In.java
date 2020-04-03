package org.morejdbc;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

class In<T> {

    private final T value;
    private final SqlType<T> type;

    private In(T value, SqlType<T> type) {
        if (value instanceof In || value instanceof Out || value instanceof InOut) {
            throw new IllegalArgumentException("incorrect call");
        }
        if (type.setter == null) {
            throw new IllegalStateException("Type " + type + " cannot be used as IN");
        }
        this.value = value;
        this.type = requireNonNull(type, "type");
    }

    static <T> In<T> of(@Nullable T value, SqlType<T> type) {
        return new In<>(value, type);
    }

    static In<Integer> of(@Nullable Integer value) {
        return of(value, SqlTypes.INTEGER);
    }

    static In<Long> of(@Nullable Long value) {
        return of(value, SqlTypes.BIGINT);
    }

    static In<BigDecimal> of(@Nullable BigDecimal value) {
        return of(value, SqlTypes.DECIMAL);
    }

    static In<String> of(@Nullable CharSequence value) {
        String strValue = value == null ? null : value.toString();
        return of(strValue, SqlTypes.VARCHAR);
    }

    static In<byte[]> of(@Nullable byte[] value) {
        return of(value, SqlTypes.BLOB);
    }

    static In<Timestamp> of(@Nullable Timestamp value) {
        return of(value, SqlTypes.TIMESTAMP);
    }

    T getValue() {
        return value;
    }

    SqlType<T> getType() {
        return type;
    }

    void beforeExecute(CallableStatement cs, int idx) throws SQLException {
        type.inBeforeExecute(cs, idx, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        In<?> that = (In<?>) o;
        return Objects.equals(value, that.value)
                && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    @Override
    public String toString() {
        return "In{" + type.getPrintName() + " " + value + "}";
    }
}
