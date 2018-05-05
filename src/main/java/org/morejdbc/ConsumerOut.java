package org.morejdbc;

import java.util.Objects;
import java.util.function.Consumer;

class ConsumerOut<T> extends AbstractOut<T> {

    private final Consumer<T> consumer;

    ConsumerOut(SqlType<T> type, Consumer<T> consumer) {
        super(type);
        this.consumer = Objects.requireNonNull(consumer, "consumer");
    }

    @Override
    void set(T value) {
        consumer.accept(value);
    }
}
