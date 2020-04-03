package org.morejdbc;

public class MockOut<T> extends Out<T> {

    Integer index;

    public MockOut(SqlType<T> type) {
        super(type);
    }

    public static <T> MockOut<T> of(SqlType<T> sqlType) {
        return new MockOut<>(sqlType);
    }

    @Override
    void onAdd(int index) {
        this.index = index;
    }

    public void setTo(Object call, T value) {
        if (call instanceof JdbcCall) {
            setTo((JdbcCall) call, value);
        } else if (call instanceof NamedJdbcCall) {
            setTo((NamedJdbcCall<?>) call, value);
        } else {
            throw new IllegalArgumentException("Unexpected mock " + call + " should be either JdbcCall or NamedJdbcCall");
        }
    }

    public void setTo(JdbcCall call, T value) {
        @SuppressWarnings("unchecked")
        AbstractOut<T> out = (AbstractOut<T>) call.parameters.get(index).out;
        out.afterExecute(value);
    }

    public void setTo(NamedJdbcCall<?> call, T value) {
        @SuppressWarnings("unchecked")
        AbstractOut<T> out = (AbstractOut<T>) call.parameters.get(index).out;
        out.afterExecute(value);
    }
}
