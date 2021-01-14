package com.lanternsoftware.util.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.apache.commons.io.IOUtils;

import com.lanternsoftware.util.NullUtils;

public class SchemaUtils {
    public static void printStatements(String _sourceCodeFolder) {
        Map<String, Class<?>> mapClasses = new TreeMap<>();
        searchFile(new File(_sourceCodeFolder), mapClasses);
        for (Class<?> c : mapClasses.values()) {
            System.out.println(SchemaUtils.generateTableCreateStatement(c));
            System.out.println(SchemaUtils.generateSequenceCreateStatement(c));
            System.out.println(SchemaUtils.generateIndexCreateStatements(c));
        }
    }

    private static void searchFile(File _f, Map<String, Class<?>> _mapClasses) {
        if (_f == null)
            return;
        if (_f.isDirectory()) {
            for (File child : _f.listFiles()) {
                searchFile(child, _mapClasses);
            }
        }
        else if (_f.getName().endsWith(".java")) {
            try {
                String sSource = IOUtils.toString(new FileInputStream(_f));
                if (!sSource.contains("@Table"))
                    return;
                int iPackagePos = sSource.indexOf("package ");
                int iPackageEnd = sSource.indexOf(";", iPackagePos);
                String sPackageName = sSource.substring(iPackagePos + 8, iPackageEnd);
                int iClassPos = sSource.indexOf("public class") + 12;
                while (sSource.charAt(iClassPos) == ' ')
                    iClassPos++;
                int iNewLineN = sSource.indexOf("\n", iClassPos);
                int iNewLineR = sSource.indexOf("\r", iClassPos);
                int iSpace = sSource.indexOf(" ", iClassPos);
                int iClassEnd = NullUtils.min((iNewLineN == -1) ? Integer.MAX_VALUE : iNewLineN, (iNewLineR == -1) ? Integer.MAX_VALUE : iNewLineR, (iSpace == -1) ? Integer.MAX_VALUE : iSpace);
                String sClassName = sSource.substring(iClassPos, iClassEnd);
                String fullName = sPackageName + "." + sClassName;
                Class<?> clazz = Class.forName(fullName);
                if (!clazz.isAnnotationPresent(Table.class))
                    return;
                _mapClasses.put(fullName, clazz);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String generateTableCreateStatement(Class<?> _entity) {
        Table table = _entity.getAnnotation(Table.class);
        if (table == null)
            return null;
        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        sql.append(table.name());
        sql.append(" (");
        boolean bFirst = true;
        for (Field f : _entity.getDeclaredFields()) {
            String name = null;
            Column column = f.getAnnotation(Column.class);
            JoinColumn join = f.getAnnotation(JoinColumn.class);
            if (column != null)
                name = column.name();
            else if (join != null)
                name = join.name();
            if (name == null)
                continue;
            StringBuilder col = new StringBuilder(name);
            col.append(" ");
            if (NullUtils.isOneOf(f.getType(), Byte.TYPE, byte.class))
                col.append("NUMBER(3,0)");
            if (NullUtils.isOneOf(f.getType(), Short.TYPE, Short.class))
                col.append("NUMBER(5,0)");
            else if (NullUtils.isOneOf(f.getType(), Integer.TYPE, Integer.class))
                col.append("NUMBER(10,0)");
            else if (NullUtils.isOneOf(f.getType(), Long.TYPE, Long.class))
                col.append("NUMBER(19,0)");
            else if (NullUtils.isOneOf(f.getType(), Double.TYPE, Double.class, Float.TYPE, Float.class))
                col.append("NUMBER(19,4)");
            else if (NullUtils.isOneOf(f.getType(), Boolean.TYPE, Boolean.class))
                col.append("NUMBER(1,0)");
            else if (f.getType().equals(String.class) || f.getType().isEnum() || (join != null)) {
                if (f.getAnnotation(Lob.class) != null)
                    col.append("CLOB");
                else
                    col.append("VARCHAR(255)");
            }
            else if (f.getType().equals(Date.class))
                col.append("TIMESTAMP");
            else
                continue;
            if (f.getAnnotation(Id.class) != null)
                col.append(" PRIMARY KEY");
            if (!bFirst)
                sql.append(",");
            else
                bFirst = false;
            sql.append(col);
        }
        sql.append(");");
        return sql.toString();
    }

    public static String generateSequenceCreateStatement(Class<?> _entity) {
        StringBuilder statements = new StringBuilder();
        for (Field f : _entity.getDeclaredFields()) {
            SequenceGenerator seq = f.getAnnotation(SequenceGenerator.class);
            if (seq == null)
                continue;
            statements.append("CREATE SEQUENCE \"");
            statements.append(seq.name());
            statements.append("\" MINVALUE 1 MAXVALUE 9999999999999999999999999999 INCREMENT BY 1 START WITH 1000 NOCACHE NOORDER NOCYCLE;\n");
        }
        return statements.toString();
    }

    public static String generateIndexCreateStatements(Class<?> _entity) {
        Table table = _entity.getAnnotation(Table.class);
        if (table == null)
            return null;
        StringBuilder statements = new StringBuilder();
        Index[] indexes = table.indexes();
        if (indexes != null) {
            for (Index index : indexes) {
                statements.append("CREATE ");
                if (index.unique())
                    statements.append("UNIQUE ");
                statements.append("INDEX \"");
                statements.append(index.name());
                statements.append("\" ON \"");
                statements.append(table.name());
                statements.append("\" (");
                statements.append(formatIndexFields(index.columnList()));
                statements.append(
                        ") PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645 PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1 BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT);\n");
            }
        }
        return statements.toString();
    }

    private static String formatIndexFields(String _fields) {
        String[] fields = NullUtils.makeNotNull(_fields).split(",");
        StringBuilder builder = null;
        if (fields != null) {
            for (String field : fields) {
                if (NullUtils.isEmpty(field))
                    continue;
                if (builder == null)
                    builder = new StringBuilder();
                else
                    builder.append(",");
                builder.append("\"");
                builder.append(field);
                builder.append("\"");
            }
        }
        if (builder == null)
            return "";
        return builder.toString();
    }

    public static String generateTablespaceCreateStatement(String _name, String _dataFilePath, int _iStartSizeMB, int _iMaxSize) {
        StringBuilder sql = new StringBuilder("CREATE TABLESPACE ");
        sql.append(_name.toUpperCase());
        sql.append(" DATAFILE '");
        sql.append(_dataFilePath);
        sql.append("' SIZE ");
        sql.append(_iStartSizeMB * 1024 * 1024);
        sql.append(" AUTOEXTEND ON NEXT 1 MAXSIZE ");
        sql.append(_iMaxSize * 1024 * 1024);
        sql.append(" BLOCKSIZE 8192 DEFAULT NOCOMPRESS ONLINE EXTENT MANAGEMENT LOCAL AUTOALLOCATE;\n");
        return sql.toString();
    }
}
