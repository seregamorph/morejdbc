package org.morejdbc;

import org.springframework.util.Assert;

import java.sql.CallableStatement;
import java.sql.SQLException;

import static java.util.Objects.requireNonNull;

abstract class AbstractOut<T> {

    final SqlType<T> type;

    boolean beforeExecuted;
    boolean afterExecuted;

    AbstractOut(SqlType<T> type) {
        this.type = requireNonNull(type, "type");
    }

    void beforeExecute(CallableStatement cs, int idx) throws SQLException {
        Assert.state(!beforeExecuted, "already beforeExecute");
        Assert.state(!afterExecuted, "value already set");
        cs.registerOutParameter(idx, type.getSqlType());
        beforeExecuted = true;
    }

    void afterExecute(CallableStatement cs, int idx) throws SQLException {
        Assert.state(beforeExecuted, "not initialized");
        Assert.state(!afterExecuted, "value already set");
        T value = type.getValueOrNull(cs, idx);
        set(value);
        afterExecuted = true;
    }

    abstract void set(T value);
}
