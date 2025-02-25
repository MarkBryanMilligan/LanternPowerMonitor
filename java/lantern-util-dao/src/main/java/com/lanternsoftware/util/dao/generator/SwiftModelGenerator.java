package com.lanternsoftware.util.dao.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.AnnotationFinder;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.annotations.CaseFormat;
import com.lanternsoftware.util.dao.annotations.DBSerializable;

public class SwiftModelGenerator {
	private static final Logger LOG = LoggerFactory.getLogger(SwiftModelGenerator.class);

	public static void generateModel(String _codePath, String _outputPath) {
		for (Entry<String, String> e : AnnotationFinder.findAnnotatedClasses(_codePath, DBSerializable.class).entrySet()) {
			generateSerializer(e.getKey(), e.getValue().replace(_codePath, _outputPath) + File.separator + "bson" + File.separator);
		}
	}

	private static SerializerGenerationResult generateSerializer(String _className, String _outputPath) {
		try {
			Set<Class<?>> customSerializerFields = new HashSet<>();
			Class<?> clazz = Class.forName(_className);
			DBSerializable dbSerializable = DaoSerializer.getAnnotation(clazz, DBSerializable.class);
			if (dbSerializable == null)
				return null;
			CaseFormat caseFormat = dbSerializable.caseFormat();

			StringBuilder bson = new StringBuilder();
			bson.append("import Foundation\nimport BSON\n\nclass ");
			bson.append(clazz.getSimpleName());
			bson.append(":LanternObject {\n");

			List<Field> fields = DaoSerializer.getSerializableFields(clazz, true);

			Map<String, List<Field>> mapFields = new HashMap<>();
			for (Field f : fields) {
				if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers()))
					continue;
				for (Annotation a : f.getAnnotations()) {
					CollectionUtils.addToMultiMap(a.annotationType().getCanonicalName(), f, mapFields);
				}
			}

			String primaryKeyName = null;
			Field primaryKey = CollectionUtils.getFirst(mapFields.get(PrimaryKey.class.getCanonicalName()));
			if (primaryKey != null) {
				primaryKeyName = AbstractDaoSerializer.fieldToDatabaseName(primaryKey, caseFormat);
			}

			for (Field f : fields) {
				bson.append("\tvar ");
				bson.append(f.getName());
				bson.append(":");
				bson.append(typeToSwift(f));
				bson.append("?\n");
			}
			bson.append("\n\tinit() {}\n\n\trequired init(bson: Document) {\n");
			for (Field f : fields) {
				bson.append("\t\tself.");
				bson.append(f.getName());
				String databaseName = AbstractDaoSerializer.fieldToDatabaseName(f, caseFormat);
				if (NullUtils.isEqual(databaseName, primaryKeyName))
					databaseName = "_id";
				if (AbstractDaoSerializer.getCollectionType(f) != null) {
					bson.append(" = BsonUtils.getList(bson:bson, field:\"");
					bson.append(databaseName);
					bson.append("\")\n");
				}
				else if (AbstractDaoSerializer.requiresCustomSerializer(f)) {
					bson.append(" = BsonUtils.getObject(bson:bson, field:\"");
					bson.append(databaseName);
					bson.append("\")\n");
				}
				else {
					bson.append(" = bson[\"");
					bson.append(databaseName);
					bson.append("\"] as? ");
					bson.append(typeToSwift(f));
					bson.append("\n");
				}
			}
			bson.append("\t}\n\n\tfunc toBSON()->Document {\n\t\tlet bson: Document = [");
			boolean first = true;
			for (Field f : fields) {
				if (!first)
					bson.append(",");
				else
					first = false;
				String databaseName = AbstractDaoSerializer.fieldToDatabaseName(f, caseFormat);
				if (NullUtils.isEqual(databaseName, primaryKeyName))
					databaseName = "_id";
				bson.append("\n\t\t\t\"");
				bson.append(databaseName);
				bson.append("\": ");
				if (AbstractDaoSerializer.getCollectionType(f) != null) {
					bson.append("BsonUtils.toDocument(coll:self.");
					bson.append(f.getName());
					bson.append(")");
				}
				else if (AbstractDaoSerializer.requiresCustomSerializer(f)) {
					bson.append("BsonUtils.toDocument(obj:self.");
					bson.append(f.getName());
					bson.append(")");
				}
				else {
					bson.append("self.");
					bson.append(f.getName());
				}
			}
			bson.append("\n\t\t]\n\t\treturn bson\n\t}\n}");

			FileOutputStream f = null;
			try {
				//                if (dbSerializable.autogen()) {
				new File(_outputPath).mkdirs();
				f = new FileOutputStream(_outputPath + clazz.getSimpleName() + ".swift");
				f.write(NullUtils.toByteArray(bson.toString()));
				//                }
				return new SerializerGenerationResult(clazz.getSimpleName() + ".swift", customSerializerFields);
			}
			catch (Exception e) {
				e.printStackTrace();
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

	private static String typeToSwift(Field _f) {
		if (Collection.class.isAssignableFrom(_f.getType()))
			return "[" + typeToSwift(AbstractDaoSerializer.getCollectionType(_f)) + "]";
		return typeToSwift(_f.getType());
	}

	private static String typeToSwift(Class<?> _class) {
		if (NullUtils.isOneOf(_class, Integer.TYPE, Integer.class))
			return "Int32";
		if (NullUtils.isOneOf(_class, Long.TYPE, Long.class))
			return "Int64";
		if (NullUtils.isOneOf(_class, Double.TYPE, Double.class))
			return "Double";
		if (NullUtils.isOneOf(_class, Float.TYPE, Float.class))
			return "Float";
		if (NullUtils.isOneOf(_class, Boolean.TYPE, Boolean.class))
			return "Bool";
		if (_class.equals(Date.class))
			return "Date";
		if (_class.equals(String.class))
			return "String";
		if (_class.equals(byte[].class))
			return "[Uint8]";
		if (_class.isEnum())
			return "String";
		if (NullUtils.isOneOf(_class, Short.TYPE, Short.class))
			return "Int16";
		if (NullUtils.isOneOf(_class, Byte.TYPE, Byte.class))
			return "Int8";
		return _class.getSimpleName();
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
