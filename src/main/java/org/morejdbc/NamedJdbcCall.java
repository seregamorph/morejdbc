package org.morejdbc;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.util.Assert;

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
 * Helper class to call stored procedures and functions with named parameters.
 * Makes the call sql itself.
 * Note, that the object cannot be reused again.
 * <p>
 * Known supported databases: Oracle and DB2.
 * Known unsupported databases: PostgreSQL, MySQL.
 * <p>
 * Usage example:
 * <pre>
 * import org.morejdbc.*;
 * import org.morejdbc.NamedJdbcCall.call;
 * import NamedJdbcCall.SqlTypes.*;
 * ...
 * private JdbcTemplate jdbcTemplate;
 * ...
 * String result = jdbcTemplate.execute(call("concat_str", VARCHAR)
 *         .in("arg1", arg1)
 *         .in("arg2", arg2));
 * </pre>
 * Example for OUT parameters:
 * <pre>
 * Out&lt;Integer&gt; sum = Out.of(INTEGER);
 * Out&lt;Long&gt; mlt = Out.of(BIGINT);
 *
 * jdbc.execute(call("test_math")
 *         .in("val1", 1)
 *         .in("val2", 2L)
 *         .out("out_sum", sum)
 *         .out("out_mlt", mlt));
 *
 * System.out.println("sum is " + sum.get());
 * System.out.println("mlt is " + mlt.get());
 * </pre>
 * Also you can use lambdas to store the result of OUT parameter:
 * <pre>
 * AtomicReference&lt;Integer&gt; sum = new AtomicReference&lt;&gt;();
 * AtomicReference&lt;Long&gt; mlt = new AtomicReference&lt;&gt;();
 *
 * jdbc.execute(call("test_math")
 *         .in("val1", 1)
 *         .in("val2", 2L)
 *         .out("out_sum", INTEGER, sum::set)
 *         .out("out_mlt", BIGINT, mlt::set));
 *
 * System.out.println("sum is " + sum.get());
 * System.out.println("mlt is " + mlt.get());
 * </pre>
 *
 * @see org.springframework.jdbc.core.SqlParameterValue
 */
public class NamedJdbcCall<T> implements ConnectionCallback<T>, SqlProvider {

    private static final Logger logger = LoggerFactory.getLogger(NamedJdbcCall.class);

    List<NamedParameter<?>> parameters = new ArrayList<>();
    private SQLExceptionHandler<T> sqlExceptionHandler;

    private final String name;
    @Nullable
    private final SqlType<T> returnType;

    private String sql;

    private NamedJdbcCall(String name, @Nullable SqlType<T> returnType) {
        this.name = requireNonNull(name, "name");
        this.returnType = returnType;
    }

    public static NamedJdbcCall<Void> call(String procedureName) {
        return new NamedJdbcCall<>(procedureName, null);
    }

    public static <T> NamedJdbcCall<T> call(String functionName, SqlType<T> returnType) {
        return new NamedJdbcCall<>(functionName, Objects.requireNonNull(returnType, "returnType"));
    }

    private NamedJdbcCall<T> in(String name, In<?> in) {
        parameters.add(new NamedParameter<>(name, requireNonNull(in, "in"), null));
        return this;
    }

    public <I> NamedJdbcCall<T> in(String name, @Nullable I inValue, SqlType<I> inType) {
        return in(name, In.of(inValue, inType));
    }

    public NamedJdbcCall<T> in(String name, @Nullable Integer value) {
        return in(name, In.of(value));
    }

    public NamedJdbcCall<T> in(String name, @Nullable Long value) {
        return in(name, In.of(value));
    }

    public NamedJdbcCall<T> in(String name, @Nullable BigDecimal value) {
        return in(name, In.of(value));
    }

    public NamedJdbcCall<T> in(String name, @Nullable CharSequence value) {
        return in(name, In.of(value));
    }

    public NamedJdbcCall<T> in(String name, @Nullable byte[] value) {
        return in(name, In.of(value));
    }

    public NamedJdbcCall<T> in(String name, @Nullable Timestamp value) {
        return in(name, In.of(value));
    }

    private NamedJdbcCall<T> outImpl(String name, AbstractOut<?> out) {
        out.onAdd(parameters.size());
        parameters.add(new NamedParameter<>(name, null, requireNonNull(out, "out")));
        return this;
    }

    public NamedJdbcCall<T> out(String name, Out<?> out) {
        return outImpl(name, out);
    }

    public <O> NamedJdbcCall<T> out(String name, SqlType<O> sqlType, Consumer<O> consumer) {
        return outImpl(name, new ConsumerOut<>(sqlType, consumer));
    }

    public <O> Out<O> out(String name, SqlType<O> type) {
        Out<O> out = Out.of(type);
        out(name, out);
        return out;
    }

    private <V> NamedJdbcCall<T> inOutImpl(String name, In<V> in, AbstractOut<V> out) {
        out.onAdd(parameters.size());
        parameters.add(new NamedParameter<>(name, requireNonNull(in, "in"), requireNonNull(out, "out")));
        return this;
    }

    private <V> NamedJdbcCall<T> inOut(String name, In<V> in, Out<V> out) {
        return inOutImpl(name, in, out);
    }

    public NamedJdbcCall<T> inOut(String name, @Nullable Integer inValue, Out<Integer> out) {
        return inOut(name, In.of(inValue), out);
    }

    public NamedJdbcCall<T> inOut(String name, @Nullable Long inValue, Out<Long> out) {
        return inOut(name, In.of(inValue), out);
    }

    public NamedJdbcCall<T> inOut(String name, @Nullable BigDecimal inValue, Out<BigDecimal> out) {
        return inOut(name, In.of(inValue), out);
    }

    public NamedJdbcCall<T> inOut(String name, @Nullable CharSequence inValue, Out<String> out) {
        return inOut(name, In.of(inValue), out);
    }

    public NamedJdbcCall<T> inOut(String name, @Nullable Timestamp inValue, Out<Timestamp> out) {
        return inOut(name, In.of(inValue), out);
    }

    private <V> NamedJdbcCall<T> inOut(String name, In<V> in, Consumer<V> outConsumer) {
        return inOutImpl(name, in, new ConsumerOut<>(in.getType(), outConsumer));
    }

    public NamedJdbcCall<T> inOut(String name, @Nullable Integer inValue, Consumer<Integer> outConsumer) {
        return inOut(name, In.of(inValue), outConsumer);
    }

    public NamedJdbcCall<T> inOut(String name, @Nullable Long inValue, Consumer<Long> outConsumer) {
        return inOut(name, In.of(inValue), outConsumer);
    }

    public NamedJdbcCall<T> inOut(String name, @Nullable BigDecimal inValue, Consumer<BigDecimal> outConsumer) {
        return inOut(name, In.of(inValue), outConsumer);
    }

    public NamedJdbcCall<T> inOut(String name, @Nullable CharSequence inValue, Consumer<String> outConsumer) {
        return inOut(name, In.of(inValue), outConsumer);
    }

    public NamedJdbcCall<T> inOut(String name, @Nullable Timestamp inValue, Consumer<Timestamp> outConsumer) {
        return inOut(name, In.of(inValue), outConsumer);
    }

    public NamedJdbcCall<T> handleException(SQLExceptionHandler<T> sqlExceptionHandler) {
        Assert.state(this.sqlExceptionHandler == null, "sqlExceptionHandler already set");
        this.sqlExceptionHandler = sqlExceptionHandler;
        return this;
    }

    static class NamedParameter<T> extends InOut<T> {

        private final String name;

        NamedParameter(String name, In<T> in, AbstractOut<T> out) {
            super(in, out);
            this.name = requireNonNull(name, "name");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            NamedParameter<?> that = (NamedParameter<?>) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), name);
        }

        @Override
        public String toString() {
            return name + " " + super.toString();
        }
    }

    @Override
    public T doInConnection(Connection conn) throws SQLException, DataAccessException {
        NamedParameter<?>[] parameters = getParameters();

        this.sql = getSql(returnType != null, parameters);
        logger.trace("sql: {}", sql);

        try (CallableStatement cs = conn.prepareCall(this.sql)) {
            @Nullable Out<T> result = returnType != null ? Out.of(returnType) : null;
            int offset;
            if (result != null) {
                offset = 1;
                result.beforeExecute(cs, 1);
            } else {
                offset = 0;
            }
            for (int i = 0; i < parameters.length; i++) {
                NamedParameter<?> parameter = parameters[i];
                parameter.beforeExecute(cs, i + offset + 1);
            }

            cs.execute();

            if (result != null) {
                result.afterExecute(cs, 1);
            }
            for (int i = 0; i < parameters.length; i++) {
                NamedParameter<?> parameter = parameters[i];
                parameter.afterExecute(cs, i + offset + 1);
            }
            return result != null ? result.get() : null;
        } catch (SQLException e) {
            if (sqlExceptionHandler != null) {
                return sqlExceptionHandler.handle(e);
            }
            throw e;
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
        NamedJdbcCall<?> that = (NamedJdbcCall<?>) o;
        return Objects.equals(parameters, that.parameters) &&
                name.equals(that.name) &&
                Objects.equals(returnType, that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters, name, returnType);
    }

    private NamedParameter<?>[] getParameters() {
        if (this.parameters == null) {
            throw new IllegalStateException("Already executed, this object cannot be reused.");
        }
        NamedParameter<?>[] parameters = this.parameters.toArray(new NamedParameter[0]);
        this.parameters = null;
        return parameters;
    }

    private String getSql(boolean function, NamedParameter<?>... parameters) {
        StringBuilder sql = new StringBuilder(function ? "{? = call " : "{call ")
                .append(name).append("(");
        for (int i = 0; i < parameters.length; i++) {
            NamedParameter<?> parameter = parameters[i];
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(parameter.name).append(" => ?");
        }
        sql.append(")}");
        return sql.toString();
    }
}
