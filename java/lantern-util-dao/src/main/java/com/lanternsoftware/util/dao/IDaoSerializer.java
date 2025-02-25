package com.lanternsoftware.util.dao;

import java.lang.annotation.Annotation;
import java.util.List;

public interface IDaoSerializer<T> {
    Class<T> getSupportedClass();
    String getTableName();
    List<String> getFieldsByAnnotation(Class<? extends Annotation> _fieldAnnotation);
    List<String> getImportantFields();
    int getSqlType(String _fieldName);
    DaoEntity toDaoEntity(T _t);
    T fromDaoEntity(DaoEntity _entity);
    List<DaoProxyType> getSupportedProxies();
    List<DaoSort> getIndexes();
}
