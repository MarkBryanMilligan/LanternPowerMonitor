package com.lanternsoftware.util.dao;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.annotations.CaseFormat;
import com.lanternsoftware.util.dao.annotations.DBClob;
import com.lanternsoftware.util.dao.annotations.DBIgnore;
import com.lanternsoftware.util.dao.annotations.DBName;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.DBType;
import com.lanternsoftware.util.dao.annotations.Important;
import com.lanternsoftware.util.dao.annotations.TimestampDates;
import com.lanternsoftware.util.dao.annotations.Unimportant;

public abstract class AbstractDaoSerializer<T> implements IDaoSerializer<T> {
    protected final Map<Class<? extends Annotation>, List<String>> annotations = new HashMap<Class<? extends Annotation>, List<String>>();
    protected final List<String> importantFields = new ArrayList<String>();
    protected final Map<String, Integer> fieldTypes = new HashMap<String, Integer>();

    public AbstractDaoSerializer() {
        addFields(getSupportedClass());
    }

    public void addFields(Class<?> _class) {
        if (_class == null) {
            return;
        }
        List<String> important = new ArrayList<String>();
        List<String> unimportant = new ArrayList<String>();
        List<String> normal = new ArrayList<String>();
        for (Field f : _class.getDeclaredFields()) {
            if (!isSerializable(f))
                continue;
            String dbName = fieldToDatabaseName(f);
            if (f.isAnnotationPresent(Important.class))
                important.add(dbName);
            else if (f.isAnnotationPresent(Unimportant.class))
                unimportant.add(dbName);
            else
                normal.add(dbName);
            Class<?> type = getType(f);
            if (NullUtils.isOneOf(f.getType(), Byte.TYPE, byte.class))
                fieldTypes.put(dbName, Types.INTEGER);
            if (NullUtils.isOneOf(f.getType(), Short.TYPE, Short.class))
                fieldTypes.put(dbName, Types.INTEGER);
            else if (NullUtils.isOneOf(f.getType(), Integer.TYPE, Integer.class))
                fieldTypes.put(dbName, Types.INTEGER);
            else if (NullUtils.isOneOf(f.getType(), Long.TYPE, Long.class))
                fieldTypes.put(dbName, Types.BIGINT);
            else if (NullUtils.isOneOf(f.getType(), Double.TYPE, Double.class, Float.TYPE, Float.class))
                fieldTypes.put(dbName, Types.DOUBLE);
            else if (NullUtils.isOneOf(f.getType(), Boolean.TYPE, Boolean.class))
                fieldTypes.put(dbName, Types.BIT);
            else if (f.getType().equals(String.class) || f.getType().isEnum()) {
                if (f.isAnnotationPresent(DBClob.class))
                    fieldTypes.put(dbName, Types.CLOB);
                else
                    fieldTypes.put(dbName, Types.VARCHAR);
            }
            else if (f.getType().equals(Date.class)) {
                if (DaoSerializer.isAnnotationPresent(_class, TimestampDates.class))
                    fieldTypes.put(dbName, Types.TIMESTAMP);
                else
                    fieldTypes.put(dbName, Types.BIGINT);
            }
            for (Annotation a : f.getAnnotations()) {
                CollectionUtils.addToMultiMap(a.annotationType(), dbName, annotations);
            }
        }
        if (!important.isEmpty())
            importantFields.addAll(important);
        else {
            normal.removeAll(unimportant);
            importantFields.addAll(normal);
        }
        addFields(_class.getSuperclass());
    }

    @Override
    public List<String> getFieldsByAnnotation(Class<? extends Annotation> _fieldAnnotation) {
        return CollectionUtils.makeNotNull(annotations.get(_fieldAnnotation));
    }

    @Override
    public List<String> getImportantFields() {
        return importantFields;
    }

    @Override
    public int getSqlType(String _fieldName) {
        Integer type = fieldTypes.get(_fieldName);
        if (type == null)
            return Types.NULL;
        return type;
    }

    @Override
    public String getTableName() {
        DBSerializable table = getSupportedClass().getAnnotation(DBSerializable.class);
        if ((table != null) && NullUtils.isNotEmpty(table.name()))
            return table.name();
        return getterNameToDatabaseName(getSupportedClass().getSimpleName());
    }

    public static String fieldToGetterName(Field _field) {
        String name = _field.getName();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static String getterNameToDatabaseName(String _name) {
        return toSnake(_name);
    }

    public static String getterNameToDatabaseName(String _name, CaseFormat _format) {
        return convertCase(_name, CaseFormat.PASCAL, _format);
    }

    public static String convertCase(String _name, CaseFormat _inFormat, CaseFormat _outFormat) {
        if (_inFormat == _outFormat)
            return _name;
        String pascal;
        if (_inFormat == CaseFormat.SNAKE)
            pascal = toPascal(_name);
        else if (_inFormat == CaseFormat.CAMEL)
            pascal = Character.toUpperCase(_name.charAt(0)) + _name.substring(1);
        else
            pascal = _name;
        if (_outFormat == CaseFormat.SNAKE)
            return toSnake(pascal);
        if (_outFormat == CaseFormat.CAMEL)
            return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
        return pascal;
    }

    private static String toPascal(String _snake) {
        StringBuilder field = new StringBuilder();
        boolean charWasWordStart = true;
        for (int i = 0; i < _snake.length(); i++) {
            if (_snake.charAt(i) == '_')
                charWasWordStart = true;
            else {
                field.append(charWasWordStart?Character.toUpperCase(_snake.charAt(i)):_snake.charAt(i));
                charWasWordStart = false;
            }
        }
        return field.toString();
    }

    private static String toSnake(String _pascal) {
        StringBuilder field = null;
        boolean charWasUpper = false;
        for (int i = 0; i < _pascal.length(); i++) {
            if (Character.isUpperCase(_pascal.charAt(i))) {
                if (field == null) {
                    field = new StringBuilder();
                    field.append(Character.toLowerCase(_pascal.charAt(i)));
                }
                else if (!charWasUpper) {
                    field.append("_");
                    field.append(Character.toLowerCase(_pascal.charAt(i)));
                }
                else {
                    field.append(Character.toLowerCase(_pascal.charAt(i)));
                }
                charWasUpper = true;
            }
            else {
                charWasUpper = false;
                if (field == null) {
                    field = new StringBuilder();
                }
                field.append(_pascal.charAt(i));
            }
        }
        return field.toString();
    }

    public static String fieldToDatabaseName(Field _field) {
        return fieldToDatabaseName(_field, CaseFormat.SNAKE);
    }

    public static String fieldToDatabaseName(Field _field, CaseFormat _format) {
        DBName name = _field.getAnnotation(DBName.class);
        if (name != null)
            return name.name();
        if (_format == CaseFormat.CAMEL)
            return _field.getName();
        return getterNameToDatabaseName(fieldToGetterName(_field), _format);
    }

    public static Class<?> getType(Field _f) {
        DBType type = _f.getAnnotation(DBType.class);
        if (type != null)
            return type.type();
        return _f.getType();
    }

    public static boolean isSerializable(Field _f) {
        return isSerializable(_f, false);
    }

    public static boolean isSerializable(Field _f, boolean _serializeObjects) {
        if (Modifier.isStatic(_f.getModifiers()) || Modifier.isTransient(_f.getModifiers()) || _f.isAnnotationPresent(DBIgnore.class))
            return false;
        if (_serializeObjects)
            return true;
        return !requiresCustomSerializer(_f);
    }

    public static boolean requiresCustomSerializer(Field _f) {
        Class<?> type = getType(_f);
        if (Collection.class.isAssignableFrom(type))
            type = getCollectionType(_f);
        return !(type.isPrimitive() || type.isEnum() || NullUtils.isOneOf(type, String.class, Date.class, BigDecimal.class, byte[].class, Boolean.class, Double.class, Long.class, Integer.class, Float.class));
    }

    public static Class<?> getCollectionType(Field _f) {
        if (Collection.class.isAssignableFrom(getType(_f)) && (_f.getGenericType() instanceof ParameterizedType)) {
            ParameterizedType t = (ParameterizedType) _f.getGenericType();
            if (t.getActualTypeArguments().length > 0) {
                Type t2 = t.getActualTypeArguments()[0];
                if (t2 instanceof Class)
                    return (Class<?>)t2;
            }
        }
        return null;
    }

    @Override
    public List<DaoProxyType> getSupportedProxies() {
        return Collections.emptyList();
    }

    @Override
    public List<DaoSort> getIndexes() {
        return null;
    }
}
