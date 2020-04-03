package org.morejdbc;

import org.springframework.util.Assert;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Objects;

class InOut<T> {

    final In<T> in;
    final AbstractOut<T> out;

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InOut<?> inOut = (InOut<?>) o;
        return Objects.equals(in, inOut.in)
                && Objects.equals(out, inOut.out);
    }

    @Override
    public int hashCode() {
        return Objects.hash(in, out);
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
