package org.morejdbc;

import java.util.function.Supplier;
import org.springframework.util.Assert;

public class Out<T> extends AbstractOut<T> implements Supplier<T> {

    private T value;

    private Out(SqlType<T> type) {
        super(type);
    }

    public static <T> Out<T> of(SqlType<T> sqlType) {
        return new Out<>(sqlType);
    }

    @Override
    void set(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        Assert.state(afterExecuted, "value was not set");
        return value;
    }

    @Override
    public String toString() {
        return "Out{" +
                "type=" + type +
                (afterExecuted ? ", value=" + value : "") +
                '}';
    }
}
