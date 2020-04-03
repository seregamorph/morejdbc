package org.morejdbc;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.SqlProvider;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Helper class to call anonymous blocks with indexed parameters.
 * Returns values in Out objects.
 */
public class JdbcCall implements ConnectionCallback<Void>, SqlProvider {

    private final String sql;

    List<InOut<?>> parameters = new ArrayList<>();

    private JdbcCall(@Language("SQL") String sql) {
        this.sql = requireNonNull(sql, "sql");
    }

    public static JdbcCall callSql(@Language("SQL") String sql) {
        return new JdbcCall(sql);
    }

    private JdbcCall in(In<?> in) {
        parameters.add(new InOut<>(in, null));
        return this;
    }

    public <I> JdbcCall in(@Nullable I inValue, SqlType<I> inType) {
        return in(In.of(inValue, inType));
    }

    public JdbcCall in(@Nullable Integer value) {
        return in(In.of(value));
    }

    public JdbcCall in(@Nullable Long value) {
        return in(In.of(value));
    }

    public JdbcCall in(@Nullable BigDecimal value) {
        return in(In.of(value));
    }

    public JdbcCall in(@Nullable CharSequence value) {
        return in(In.of(value));
    }

    public JdbcCall in(@Nullable byte[] value) {
        return in(In.of(value));
    }

    public JdbcCall in(@Nullable Timestamp value) {
        return in(In.of(value));
    }

    private JdbcCall outImpl(AbstractOut<?> out) {
        out.onAdd(parameters.size());
        parameters.add(new InOut<>(null, requireNonNull(out, "out")));
        return this;
    }

    public JdbcCall out(Out<?> out) {
        return outImpl(out);
    }

    public <O> JdbcCall out(SqlType<O> sqlType, Consumer<O> consumer) {
        return outImpl(new ConsumerOut<>(sqlType, consumer));
    }

    public <O> Out<O> out(SqlType<O> type) {
        Out<O> out = Out.of(type);
        out(out);
        return out;
    }

    @Override
    public Void doInConnection(Connection conn) throws SQLException, DataAccessException {
        InOut[] parameters = getParameters();

        try (CallableStatement cs = conn.prepareCall(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                InOut parameter = parameters[i];
                parameter.beforeExecute(cs, i + 1);
            }

            cs.execute();

            for (int i = 0; i < parameters.length; i++) {
                InOut parameter = parameters[i];
                parameter.afterExecute(cs, i + 1);
            }

            return null;
        }
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JdbcCall that = (JdbcCall) o;
        return Objects.equals(sql, that.sql) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sql, parameters);
    }

    private InOut[] getParameters() {
        InOut[] parameters = this.parameters.toArray(new InOut[0]);
        this.parameters = null;
        return parameters;
    }
}
