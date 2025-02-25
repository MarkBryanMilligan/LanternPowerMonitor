package com.lanternsoftware.util.dao.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.AnnotationFinder;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.CaseFormat;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;
import com.lanternsoftware.util.dao.annotations.StringDates;
import com.lanternsoftware.util.dao.annotations.TimestampDates;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.ITransformer;
import com.lanternsoftware.util.NullUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaoSerializerGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DaoSerializerGenerator.class);

    public static void generateSerializers(String _codePath) {
        generateSerializers(_codePath, false, null);
    }

    public static void generateSerializers(String _codePath, boolean _serializeNestedObjects, List<DaoProxyType> _proxyTypes) {
        generateSerializers(_codePath, _serializeNestedObjects, _proxyTypes, true);
    }

    public static void generateSerializers(String _codePath, boolean _serializeNestedObjects, List<DaoProxyType> _proxyTypes, boolean _generateSpiFile) {
        if (CollectionUtils.isEmpty(_proxyTypes))
            _proxyTypes = Collections.singletonList(DaoProxyType.MONGO);
        Map<String, List<String>> serializers = new HashMap<>();
        for (Map.Entry<String, String> e : AnnotationFinder.findAnnotatedClasses(_codePath, DBSerializable.class).entrySet()) {
            SerializerGenerationResult result = generateSerializer(e.getKey(), e.getValue() + File.separator + "dao" + File.separator, _serializeNestedObjects, null, _proxyTypes, null);
            if (result == null)
                continue;
            int idx = e.getValue().indexOf(File.separator + "src" + File.separator);
            if (idx > -1)
                CollectionUtils.addToMultiMap(e.getValue().substring(0, idx + 9) + File.separator + "resources" + File.separator + "META-INF" + File.separator + "services" + File.separator, result.getClassName(), serializers);
        }
        if (_generateSpiFile) {
            for (Entry<String, List<String>> entry : serializers.entrySet()) {
                new File(entry.getKey()).mkdirs();
                FileOutputStream f = null;
                try {
                    f = new FileOutputStream(entry.getKey() + "com.lanternsoftware.util.dao.IDaoSerializer");
                    Collections.sort(entry.getValue());
                    for (String className : entry.getValue()) {
                        f.write(NullUtils.toByteArray(className));
                        f.write(NullUtils.toByteArray("\r\n"));
                    }
                }
                catch (Exception e) {
                    LOG.error("Failed to create service loader file", e);
                }
                finally {
                    IOUtils.closeQuietly(f);
                }
            }
        }
    }

    /**
     * @param _className
     *            The name of the class to be serialized
     * @param _outputPath
     *            The path to write the generated serializer to
     * @param _serializeNestedObjects
     *            if true, the serializer will save a hierarchical structure containing all sub objects. If false, it only serializes primitives
     * @param _fieldNameExceptions
     *            a mapping of standard names to actual names to handle special cases where existing objects aren't following proper naming conventions
     * @param _intendedProxyTypes
     *            a list of proxy types that can use this generated serializer (can pass null if it should be used for all proxies)
     * @param _primaryKey
     *            The database primary key field. For Mongo, this will be changed to "_id" in the serializer.
     * @return The class name of the generated serializer
     */
    public static SerializerGenerationResult generateSerializer(String _className, String _outputPath, boolean _serializeNestedObjects, Map<String, String> _fieldNameExceptions, List<DaoProxyType> _intendedProxyTypes, String _primaryKey) {
        try {
            Set<Class<?>> customSerializerFields = new HashSet<>();
            Class<?> clazz = Class.forName(_className);
            String packagePath;
            int srcPos = _outputPath.indexOf(File.separator + "java" + File.separator);
            if (srcPos > -1) {
                packagePath = _outputPath.substring(srcPos + 6).replace(File.separator, ".");
                if (packagePath.endsWith("."))
                    packagePath = packagePath.substring(0, packagePath.length() - 1);
            }
            else
                packagePath = clazz.getPackage().getName() + ".dao";
            StringBuilder serializer = new StringBuilder();
            serializer.append("package ");
            serializer.append(packagePath);
            serializer.append(";\n\n");

            Set<Class<?>> imports = CollectionUtils.asHashSet(AbstractDaoSerializer.class, DaoEntity.class, DaoSerializer.class, clazz);
            if (CollectionUtils.isNotEmpty(_intendedProxyTypes)) {
                imports.add(DaoProxyType.class);
                imports.add(List.class);
                if (_intendedProxyTypes.size() > 1)
                    imports.add(Arrays.class);
                else
                    imports.add(Collections.class);
            }

            Map<String, List<Field>> mapFields = new HashMap<>();
            List<Field> fields = DaoSerializer.getSerializableFields(clazz, _serializeNestedObjects);
            for (Field f : fields) {
                for (Annotation a : f.getAnnotations()) {
                    CollectionUtils.addToMultiMap(a.annotationType().getCanonicalName(), f, mapFields);
                }
                Class<?> type = AbstractDaoSerializer.getType(f);
                if (Collection.class.isAssignableFrom(type)) {
                    Class<?> elementClass = ((Class<?>) (((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]));
                    imports.add(elementClass);
                }
                else if (type.isEnum() || AbstractDaoSerializer.requiresCustomSerializer(f))
                    imports.add(type);
            }
            CaseFormat caseFormat = CaseFormat.SNAKE;
            DBSerializable dbSerializable = DaoSerializer.getAnnotation(clazz, DBSerializable.class);
            if ((dbSerializable != null) && (dbSerializable.caseFormat() != null))
                caseFormat = dbSerializable.caseFormat();
            StringDates dateFormat = DaoSerializer.getAnnotation(clazz, StringDates.class);
            if (dateFormat != null)
                imports.add(DateUtils.class);

            Field primaryKey = CollectionUtils.getFirst(mapFields.get(PrimaryKey.class.getCanonicalName()));
            if (primaryKey != null) {
                if (primaryKey.getType() != String.class)
                    imports.add(NullUtils.class);
                if (NullUtils.isEmpty(_primaryKey))
                    _primaryKey = AbstractDaoSerializer.fieldToDatabaseName(primaryKey, caseFormat);
            }

            List<String> imp = CollectionUtils.transform(imports, new ITransformer<Class<?>, String>() {
                @Override
                public String transform(Class<?> _class) {
                    return "import " + _class.getCanonicalName() + ";\n";
                }
            });
            Collections.sort(imp);
            for (String i : imp) {
                serializer.append(i);
            }
            serializer.append("\npublic class ");
            serializer.append(clazz.getSimpleName());
            serializer.append("Serializer extends AbstractDaoSerializer<");
            serializer.append(clazz.getSimpleName());
            serializer.append(">\n{\n");
            if (dateFormat != null) {
                serializer.append("\tprivate static final String FORMAT = \"");
                serializer.append(dateFormat.format());
                serializer.append("\";\n\n");
            }
            serializer.append("\t@Override\n\tpublic Class<");
            serializer.append(clazz.getSimpleName());
            serializer.append("> getSupportedClass()\n\t{\n\t\treturn ");
            serializer.append(clazz.getSimpleName());
            serializer.append(".class;\n\t}\n\n");
            String intendedType = "DaoProxyType." + CollectionUtils.getFirst(_intendedProxyTypes).name();
            if (CollectionUtils.isNotEmpty(_intendedProxyTypes)) {
                serializer.append("\t@Override\n\tpublic List<DaoProxyType> getSupportedProxies() {\n\t\t");
                if (_intendedProxyTypes.size() > 1) {
                    serializer.append("return Arrays.asList(");
                    serializer.append(CollectionUtils.transformToCommaSeparated(_intendedProxyTypes, new ITransformer<DaoProxyType, String>() {
                        @Override
                        public String transform(DaoProxyType _daoProxyType) {
                            return "DaoProxyType." + _daoProxyType.name();
                        }
                    }));
                    serializer.append(");\n\t}\n");
                }
                else {
                    serializer.append("return Collections.singletonList(");
                    serializer.append(intendedType);
                    serializer.append(");\n\t}\n\n");
                }
            }
            serializer.append("\t@Override\n\tpublic DaoEntity toDaoEntity(");
            serializer.append(clazz.getSimpleName());
            serializer.append(" _o)\n\t{\n\t\tDaoEntity d = new DaoEntity(");
            serializer.append(");\n");

            StringBuilder from = new StringBuilder();
            from.append("\t@Override\n\tpublic ");
            from.append(clazz.getSimpleName());
            from.append(" fromDaoEntity(DaoEntity _d)\n\t{\n\t\t");
            from.append(clazz.getSimpleName());
            from.append(" o = new ");
            from.append(clazz.getSimpleName());
            from.append("();\n");
            for (Field f : fields) {
                String databaseField = AbstractDaoSerializer.fieldToDatabaseName(f, caseFormat);
                boolean customSerializer = AbstractDaoSerializer.requiresCustomSerializer(f);
                Class<?> type = AbstractDaoSerializer.getType(f);
                String classField = AbstractDaoSerializer.fieldToGetterName(f);
                if (_fieldNameExceptions != null) {
                    String override = _fieldNameExceptions.get(classField);
                    if (NullUtils.isNotEmpty(override))
                        classField = override;
                }
                Class<?> collType = null;
                if (Collection.class.isAssignableFrom(type))
                    collType = ((Class<?>) (((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]));

                boolean mongoPrimaryKey = NullUtils.isEqual(databaseField, _primaryKey) && _intendedProxyTypes.contains(DaoProxyType.MONGO);
                if (mongoPrimaryKey) {
                    databaseField = "_id";
                    serializer.append("\t\tif (_o.get");
                    serializer.append(classField);
                    serializer.append("() != null)");
                    serializer.append("\n\t\t\td.put(\"");
                    serializer.append(databaseField);
                    serializer.append("\", _o.get");
                    serializer.append(classField);
                    from.append("\t\to.set");
                    from.append(classField);
                    serializer.append("());\n");
                    from.append("(DaoSerializer.getString(_d, \"");
                    from.append(databaseField);
                    from.append("\"));\n");
                }
                else {
                    serializer.append("\t\td.put(\"");
                    serializer.append(databaseField);
                    if (customSerializer) {
                        if (Collection.class.isAssignableFrom(type)) {
                            serializer.append("\", DaoSerializer.toDaoEntities(_o.get");
                            serializer.append(classField);
                            customSerializerFields.add(collType);
                        }
                        else {
                            serializer.append("\", DaoSerializer.toDaoEntity(_o.get");
                            customSerializerFields.add(type);
                            serializer.append(classField);
                        }
                        if (NullUtils.isEmpty(intendedType))
                            serializer.append("()));\n");
                        else {
                            serializer.append("(), ");
                            serializer.append(intendedType);
                            serializer.append("));\n");
                        }
                    }
                    else if (type.equals(Date.class)) {
                        if (dateFormat != null) {
                            serializer.append("\", DateUtils.format(FORMAT, _o.get");
                        }
                        else if (DaoSerializer.isAnnotationPresent(clazz, TimestampDates.class))
                            serializer.append("\", DaoSerializer.toTimestamp(_o.get");
                        else
                            serializer.append("\", DaoSerializer.toLong(_o.get");
                        serializer.append(classField);
                        serializer.append("()));\n");
                    }
                    else if (type.isEnum()) {
                        serializer.append("\", DaoSerializer.toEnumName(_o.get");
                        serializer.append(classField);
                        serializer.append("()));\n");
                    }
                    else {
                        if (type.getName().equals("boolean")) {
                            serializer.append("\", _o.is");
                        }
                        else {
                            serializer.append("\", _o.get");
                        }
                        serializer.append(classField);
                        serializer.append("());\n");
                    }
                    from.append("\t\to.set");
                    from.append(classField);

                    from.append("(DaoSerializer.get");
                    if (type.getName().equals("int")) {
                        from.append("Integer");
                    }
                    else if (type.equals(Date.class)) {
                        from.append("Date");
                    }
                    else if (type.equals(String.class)) {
                        from.append("String");
                    }
                    else if (type.equals(BigDecimal.class)) {
                        from.append("BigDecimal");
                    }
                    else if (type.equals(byte[].class)) {
                        from.append("ByteArray");
                    }
                    else if (type.isEnum()) {
                        from.append("Enum");
                    }
                    else if (customSerializer) {
                        if (Collection.class.isAssignableFrom(type))
                            from.append("List");
                        else
                            from.append("Object");
                    }
                    else {
                        from.append(type.getSimpleName().substring(0, 1).toUpperCase());
                        from.append(type.getSimpleName().substring(1));
                    }
                    from.append("(_d, \"");
                    from.append(databaseField);
                    from.append("\"");
                    if (type.equals(Date.class) && (dateFormat != null))
                        from.append(", FORMAT");
                    else if (Collection.class.isAssignableFrom(type)) {
                        from.append(", ");
                        from.append(collType.getSimpleName());
                        from.append(".class");
                    }
                    else if (type.isEnum() || customSerializer) {
                        from.append(", ");
                        from.append(type.getSimpleName());
                        from.append(".class");
                    }
                    from.append("));\n");
                }
            }
            serializer.append("\t\treturn d;\n\t}\n\n");
            serializer.append(from.toString());
            serializer.append("\t\treturn o;\n\t}\n}");

            FileOutputStream f = null;
            try {
                if ((dbSerializable == null) || dbSerializable.autogen()) {
                    new File(_outputPath).mkdirs();
                    f = new FileOutputStream(_outputPath + clazz.getSimpleName() + "Serializer.java");
                    f.write(NullUtils.toByteArray(serializer.toString()));
                }
                return new SerializerGenerationResult(packagePath + "." + clazz.getSimpleName() + "Serializer", customSerializerFields);
            }
            catch (Exception e) {
                LOG.error("Failed to write serializer", e);
                return null;
            }
            finally {
                IOUtils.closeQuietly(f);
            }
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    private static boolean doesImplement(Class<?> _class, String _interfaceName) {
        if ((_class == null) || _class.equals(Object.class))
            return false;
        for (Class<?> intf : _class.getInterfaces()) {
            if (intf.getSimpleName().equals(_interfaceName))
                return true;
            if (doesImplement(intf, _interfaceName))
                return true;
        }
        return doesImplement(_class.getSuperclass(), _interfaceName);
    }
}
