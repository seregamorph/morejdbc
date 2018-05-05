package org.morejdbc;

import org.springframework.jdbc.core.namedparam.AbstractSqlParameterSource;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * https://asktom.oracle.com/pls/asktom/f?p=100:11:0::::P11_QUESTION_ID:9497064796920
 */
public class SqlTypedParameterSource extends AbstractSqlParameterSource {

    private final Map<String, Object> values = new LinkedHashMap<>();

    public SqlTypedParameterSource addValue(String name, In<?> in) {
        this.values.put(name, in.getValue());
        registerSqlType(name, in.getType().getSqlType());
        return this;
    }

    public SqlTypedParameterSource addValue(String name, @Nullable Integer value) {
        return addValue(name, In.of(value));
    }

    public SqlTypedParameterSource addValue(String name, @Nullable Long value) {
        return addValue(name, In.of(value));
    }

    public SqlTypedParameterSource addValue(String name, @Nullable BigDecimal value) {
        return addValue(name, In.of(value));
    }

    public SqlTypedParameterSource addValue(String name, @Nullable CharSequence value) {
        return addValue(name, In.of(value));
    }

    public SqlTypedParameterSource addValue(String name, @Nullable Timestamp value) {
        return addValue(name, In.of(value));
    }

    @Override
    public boolean hasValue(String name) {
        return this.values.containsKey(name);
    }

    @Override
    public Object getValue(String name) throws IllegalArgumentException {
        if (!hasValue(name)) {
            throw new IllegalArgumentException("No value registered for key '" + name + "'");
        }
        return this.values.get(name);
    }
}
