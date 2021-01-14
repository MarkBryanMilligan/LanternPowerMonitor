package com.lanternsoftware.util.dao.generator;

import java.util.Set;

public class SerializerGenerationResult {
    private final String className;
    private final Set<Class<?>> fieldsNeedingCustomSerializers;

    public SerializerGenerationResult(String _className, Set<Class<?>> _fieldsNeedingCustomSerializers) {
        className = _className;
        fieldsNeedingCustomSerializers = _fieldsNeedingCustomSerializers;
    }

    public String getClassName() {
        return className;
    }

    public Set<Class<?>> getFieldsNeedingCustomSerializers() {
        return fieldsNeedingCustomSerializers;
    }
}
