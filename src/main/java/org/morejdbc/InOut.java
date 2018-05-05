package org.morejdbc;

import org.springframework.util.Assert;

import java.sql.CallableStatement;
import java.sql.SQLException;

class InOut<T> {

    private final In<T> in;
    private final AbstractOut<T> out;

    InOut(In<T> in, AbstractOut<T> out) {
        Assert.isTrue(in != null || out != null, "Either in or out should be not null");
        this.in = in;
        this.out = out;
    }

    void beforeExecute(CallableStatement cs, int idx) throws SQLException {
        if (in != null) {
            in.beforeExecute(cs, idx);
        }
        if (out != null) {
            out.beforeExecute(cs, idx);
        }
    }

    void afterExecute(CallableStatement cs, int idx) throws SQLException {
        if (out != null) {
            out.afterExecute(cs, idx);
        }
    }

    @Override
    public String toString() {
        if (in != null && out != null) {
            return "(" + in + "," + out + ")";
        }

        if (in != null) {
            return in.toString();
        } else {
            return out.toString();
        }
    }
}
