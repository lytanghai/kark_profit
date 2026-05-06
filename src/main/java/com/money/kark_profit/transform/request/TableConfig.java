package com.money.kark_profit.transform.request;

import java.util.List;
import java.util.function.Supplier;

import static com.money.kark_profit.service.feature.BackupService.getFieldNames;

public class TableConfig {
    private final Supplier<List<?>> queryFunction;
    private final Class<?> modelClass;

    public TableConfig(Supplier<List<?>> queryFunction, Class<?> modelClass) {
        this.queryFunction = queryFunction;
        this.modelClass = modelClass;
    }

    public List<?> fetch() {
        return queryFunction.get();
    }

    public String[] getFields() {
        return getFieldNames(modelClass);
    }
}