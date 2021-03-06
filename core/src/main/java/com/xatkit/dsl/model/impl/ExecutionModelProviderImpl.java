package com.xatkit.dsl.model.impl;

import com.xatkit.dsl.model.ExecutionModelProvider;
import com.xatkit.execution.ExecutionModel;
import lombok.NonNull;

public class ExecutionModelProviderImpl implements ExecutionModelProvider {

    protected ExecutionModel model;

    public ExecutionModelProviderImpl(@NonNull ExecutionModel model) {
        this.model = model;
    }

    @Override
    public @NonNull ExecutionModel getExecutionModel() {
        return this.model;
    }
}
