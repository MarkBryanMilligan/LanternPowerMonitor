package com.lanternsoftware.util.dao.generator;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.AnnotationFinder;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.DBClob;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;
import com.lanternsoftware.util.dao.annotations.TimestampDates;

public class SchemaGenerator {
    public static void generateSchema(String _sourceCodeFolder) {
        Map<String, String> classes = AnnotationFinder.findAnnotatedClasses(_sourceCodeFolder, DBSerializable.class);
        for (String className : classes.keySet()) {
            try {
                Class<?> clazz = Class.forName(className);
                if ((clazz == null) || !DaoSerializer.isAnnotationPresent(clazz, DBSerializable.class))
                    continue;
                System.out.println(generateTableCreateStatement(clazz));
            }
            catch (ClassNotFoundException _e) {
            }
        }
    }

    public static String generateTableCreateStatement(Class<?> _entity) {
        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        boolean timestampDates = _entity.getAnnotation(TimestampDates.class) != null;
        String tableName = DaoSerializer.getTableName(_entity);
        sql.append(tableName);
        sql.append(" (");
        boolean bFirst = true;
        List<String> keys = DaoSerializer.getFieldsByAnnotation(_entity, PrimaryKey.class);
        for (Field f : DaoSerializer.getSerializableFields(_entity)) {
            String name = AbstractDaoSerializer.fieldToDatabaseName(f);
            if (name == null)
                continue;
            StringBuilder col = new StringBuilder(name);
            col.append(" ");
            if (NullUtils.isOneOf(f.getType(), Byte.TYPE, byte.class))
                col.append("NUMBER(3,0)");
            else if (NullUtils.isOneOf(f.getType(), Short.TYPE, Short.class))
                col.append("NUMBER(5,0)");
            else if (NullUtils.isOneOf(f.getType(), Integer.TYPE, Integer.class))
                col.append("NUMBER(10,0)");
            else if (NullUtils.isOneOf(f.getType(), Long.TYPE, Long.class))
                col.append("NUMBER(19,0)");
            else if (NullUtils.isOneOf(f.getType(), Double.TYPE, Double.class, Float.TYPE, Float.class))
                col.append("NUMBER(19,4)");
            else if (NullUtils.isOneOf(f.getType(), Boolean.TYPE, Boolean.class))
                col.append("NUMBER(1,0)");
            else if (f.getType().equals(String.class) || f.getType().isEnum()) {
                if (f.getAnnotation(DBClob.class) != null)
                    col.append("CLOB");
                else
                    col.append("VARCHAR(255)");
            }
            else if (f.getType().equals(Date.class)) {
                if (timestampDates)
                    col.append("TIMESTAMP");
                else
                    col.append("NUMBER(19,0)");
            }
            else
                continue;
            if ((f.getAnnotation(PrimaryKey.class) != null) && (keys.size() == 1))
                col.append(" PRIMARY KEY");
            if (!bFirst)
                sql.append(",");
            else
                bFirst = false;
            sql.append(col);
        }
        if (keys.size() > 1) {
            sql.append(", CONSTRAINT ");
            sql.append(tableName);
            sql.append("_pk PRIMARY KEY (");
            sql.append(CollectionUtils.commaSeparated(keys));
            sql.append(")");
        }
        sql.append(");");
        return sql.toString();
    }
}
