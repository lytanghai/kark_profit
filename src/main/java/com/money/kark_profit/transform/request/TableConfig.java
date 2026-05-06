package com.money.kark_profit.transform.request;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
public class TableConfig<T> {

    private final Supplier<List<T>> queryFunction;
    private final List<Field> fields;

    public TableConfig(Supplier<List<T>> queryFunction, Class<T> modelClass) {
        this.queryFunction = queryFunction;
        this.fields = Arrays.stream(modelClass.getDeclaredFields())
                .peek(f -> f.setAccessible(true))
                .toList();
    }

    public List<T> fetch() {
        return queryFunction.get();
    }

    public List<Field> getFields() {
        return fields;
    }
}