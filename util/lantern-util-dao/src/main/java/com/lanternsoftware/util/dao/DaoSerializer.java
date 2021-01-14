package com.lanternsoftware.util.dao;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.bson.BsonBinaryReader;
import org.bson.BsonBinaryWriter;
import org.bson.Document;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.IterableCodec;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.io.BasicOutputBuffer;
import org.bson.json.Converter;
import org.bson.json.JsonReader;
import org.bson.json.JsonWriterSettings;
import org.bson.json.StrictJsonWriter;
import org.bson.types.Binary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.ITransformer;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ZipUtils;
import com.lanternsoftware.util.dao.annotations.DBIndex;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;

public class DaoSerializer {
    private static final Logger LOG = LoggerFactory.getLogger(DaoSerializer.class);
    private static final Map<Class<?>, List<IDaoSerializer>> serializers = new HashMap<>();

    static {
        for (IDaoSerializer serializer : ServiceLoader.load(IDaoSerializer.class)) {
            CollectionUtils.addToMultiMap(serializer.getSupportedClass(), serializer, serializers);
        }
    }

    public static void addSerializer(IDaoSerializer<?> _serializer) {
        CollectionUtils.addToMultiMap(_serializer.getSupportedClass(), _serializer, serializers);
    }

    public static <T> IDaoSerializer<T> getSerializer(Class<T> _class) {
        return getSerializer(_class, null);
    }

    public static <T> IDaoSerializer<T> getSerializer(Class<T> _class, DaoProxyType _proxyType) {
        List<IDaoSerializer> classSerializers = serializers.get(_class);
        if (classSerializers == null) {
            LOG.error("No serializer exists for class " + _class.getCanonicalName());
            return null;
        }
        if (_proxyType != null) {
            for (IDaoSerializer serializer : classSerializers) {
                if (serializer.getSupportedProxies().contains(_proxyType))
                    return serializer;
            }
        }
        return CollectionUtils.getFirst(classSerializers);
    }

    public static DaoEntity toDaoEntity(Object _o) {
        return toDaoEntity(_o, null);
    }

    public static DaoEntity toDaoEntity(Object _o, DaoProxyType _proxyType) {
        if (_o == null) {
            return null;
        }
        if (_o instanceof DaoEntity)
            return (DaoEntity) _o;
        if (_o instanceof Document)
            return new DaoEntity((Document) _o);
        IDaoSerializer serializer = getSerializer(_o.getClass(), _proxyType);
        if (serializer == null)
            return null;
        try {
            return serializer.toDaoEntity(_o);
        }
        catch (Exception _e) {
            LOG.error("Failed to serialize entity", _e);
            return null;
        }
    }

    public static <T> T fromDaoEntity(DaoEntity _entity, Class<T> _class) {
        return fromDaoEntity(_entity, _class, null);
    }

    public static <T> T fromDaoEntity(DaoEntity _entity, Class<T> _class, DaoProxyType _proxyType) {
        if (_entity == null)
            return null;
        if (_class == DaoEntity.class)
            return _class.cast(_entity);
        if (_class == DaoQuery.class)
            return _class.cast(new DaoQuery(_entity));
        IDaoSerializer<T> serializer = getSerializer(_class, _proxyType);
        if (serializer == null)
            return null;
        return serializer.fromDaoEntity(_entity);
    }

    public static String getTableName(Class<?> _class) {
        return getTableName(_class, null);
    }

    public static String getTableName(Class<?> _class, DaoProxyType _proxyType) {
        IDaoSerializer<?> serializer = getSerializer(_class, _proxyType);
        if (serializer == null)
            return null;
        return serializer.getTableName();
    }

    public static List<String> getFieldsByAnnotation(Class<?> _entityClass, Class<? extends Annotation> _fieldAnnotation) {
        return getFieldsByAnnotation(_entityClass, _fieldAnnotation, null);
    }

    public static List<String> getFieldsByAnnotation(Class<?> _entityClass, Class<? extends Annotation> _fieldAnnotation, DaoProxyType _proxyType) {
        if (_entityClass == null) {
            return Collections.emptyList();
        }
        IDaoSerializer<?> serializer = getSerializer(_entityClass, _proxyType);
        if (serializer == null)
            return Collections.emptyList();
        return serializer.getFieldsByAnnotation(_fieldAnnotation);
    }

    public static List<String> getImportantFields(Class<?> _entityClass) {
        if (_entityClass == null) {
            return Collections.emptyList();
        }
        IDaoSerializer<?> serializer = getSerializer(_entityClass, null);
        if (serializer == null)
            return Collections.emptyList();
        return serializer.getImportantFields();
    }

    public static int getSqlType(Class<?> _entityClass, String _fieldName) {
        if (_entityClass == null) {
            return Types.NULL;
        }
        IDaoSerializer<?> serializer = getSerializer(_entityClass, DaoProxyType.JDBC);
        if (serializer == null) {
            return Types.NULL;
        }
        return serializer.getSqlType(_fieldName);
    }

    public static int compare(DaoEntity _e, String _field, Object _comp) {
        if (_comp instanceof String)
            return NullUtils.compare(getString(_e, _field), (String) _comp);
        if (_comp instanceof Date)
            return NullUtils.compare(getDate(_e, _field), (Date) _comp);
        if (_comp instanceof Long)
            return NullUtils.compare(getLong(_e, _field), (Long) _comp);
        if (_comp instanceof Short)
            return NullUtils.compare(getShort(_e, _field), (Short) _comp);
        if (_comp instanceof BigDecimal)
            return NullUtils.compare(getBigDecimal(_e, _field), (BigDecimal) _comp);
        if (_comp instanceof Double)
            return NullUtils.compare(getDouble(_e, _field), (Double) _comp);
        if (_comp instanceof Float)
            return NullUtils.compare(getFloat(_e, _field), (Float) _comp);
        if (_comp instanceof Integer)
            return NullUtils.compare(getInteger(_e, _field), (Integer) _comp);
        if (_comp instanceof Boolean)
            return NullUtils.compare(getBoolean(_e, _field), (Boolean) _comp);
        if (_comp instanceof Enum)
            return NullUtils.compare(getString(_e, _field), ((Enum) _comp).name());
        return 0;
    }

    public static String getString(DaoEntity _e, String _field) {
        if (_e == null) {
            return null;
        }
        return toString(_e.get(_field));
    }

    public static String toString(Object _o) {
        if (_o instanceof String)
            return (String) _o;
        if (_o != null)
            return String.valueOf(_o);
        return null;
    }

    public static String getId(DaoEntity _e, Class<?> _entityClass) {
        if (_e == null) {
            return null;
        }
        String sPrimaryKeyField = CollectionUtils.getFirst(DaoSerializer.getFieldsByAnnotation(_entityClass, PrimaryKey.class));
        if (NullUtils.isEmpty(sPrimaryKeyField)) {
            sPrimaryKeyField = "_id";
        }
        return getString(_e, sPrimaryKeyField);
    }

    public static Date getDate(DaoEntity _e, String _field, long _lNullValue) {
        if ((_e == null) || (!_e.containsKey(_field))) {
            return null;
        }
        Object o = _e.get(_field);
        if (o == null) {
            return null;
        }
        if (o instanceof Timestamp) {
            return new Date(((Timestamp) o).getTime());
        }
        if (o instanceof Date) {
            return (Date) o;
        }
        long lDate = toLong(o);
        if (lDate == _lNullValue) {
            return null;
        }
        return new Date(lDate);
    }

    public static Date getDate(DaoEntity _e, String _sField) {
        return getDate(_e, _sField, Long.MIN_VALUE);
    }

    public static Date getDate(DaoEntity _e, String _sField, String _format) {
        return DateUtils.parse(_format, getString(_e, _sField));
    }

    public static Timestamp toTimestamp(Date _dt) {
        if (_dt == null) {
            return null;
        }
        return new Timestamp(_dt.getTime());
    }

    public static long toLong(Date _dt) {
        return toLong(_dt, Long.MIN_VALUE);
    }

    public static long toLong(Date _dt, long _lNullValue) {
        if (_dt == null) {
            return _lNullValue;
        }
        return _dt.getTime();
    }

    public static short getShort(DaoEntity _e, String _sField) {
        if (_e == null) {
            return 0;
        }
        return toShort(_e.get(_sField));
    }

    public static short toShort(Object _o) {
        try {
            if (_o instanceof Short) {
                return (Short) _o;
            }
            if (_o instanceof Integer) {
                return ((Integer) _o).shortValue();
            }
            if (_o instanceof Long) {
                return ((Long) _o).shortValue();
            }
            if (_o instanceof Double) {
                return ((Double) _o).shortValue();
            }
            if (_o instanceof Boolean) {
                return ((Boolean) _o) ? (short) 1 : (short) 0;
            }
            if (_o instanceof String) {
                return Short.valueOf((String) _o);
            }
            return (short) 0;
        }
        catch (Exception _e) {
            return (short) 0;
        }
    }

    public static BigDecimal getBigDecimal(DaoEntity _e, String _sField) {
        if (_e == null) {
            return new BigDecimal(0);
        }
        return toBigDecimal(_e.get(_sField));
    }

    public static BigDecimal toBigDecimal(Object _o) {
        try {
            if (_o instanceof BigDecimal) {
                return (BigDecimal) _o;
            }
            if (_o instanceof Double) {
                return new BigDecimal((Double) _o);
            }
            if (_o instanceof Integer) {
                return new BigDecimal((Integer) _o);
            }
            if (_o instanceof Short) {
                return new BigDecimal((Short) _o);
            }
            if (_o instanceof Long) {
                return new BigDecimal((Long) _o);
            }
            if (_o instanceof Boolean) {
                return new BigDecimal(((Boolean) _o) ? 1 : 0);
            }
            if (_o instanceof String) {
                return new BigDecimal((String) _o);
            }
            return new BigDecimal(0);
        }
        catch (Exception _e) {
            return new BigDecimal(0);
        }
    }

    public static double getDouble(DaoEntity _e, String _sField) {
        if (_e == null) {
            return 0.0;
        }
        return toDouble(_e.get(_sField));
    }

    public static double toDouble(Object _o) {
        try {
            if (_o instanceof Double) {
                return ((Double) _o).doubleValue();
            }
            if (_o instanceof BigDecimal) {
                return ((BigDecimal) _o).doubleValue();
            }
            if (_o instanceof Integer) {
                return ((Integer) _o).doubleValue();
            }
            if (_o instanceof Short) {
                return ((Short) _o).doubleValue();
            }
            if (_o instanceof Long) {
                return ((Long) _o).doubleValue();
            }
            if (_o instanceof Boolean) {
                return ((Boolean) _o) ? 1.0 : 0.0;
            }
            if (_o instanceof String) {
                return Double.valueOf((String) _o);
            }
            return 0.0;
        }
        catch (Exception _e) {
            return 0.0;
        }
    }

    public static float getFloat(DaoEntity _e, String _sField) {
        if (_e == null) {
            return 0.0f;
        }
        return toFloat(_e.get(_sField));
    }

    public static float toFloat(Object _o) {
        try {
            if (_o instanceof Float) {
                return ((Float) _o).floatValue();
            }
            if (_o instanceof Double) {
                return ((Double) _o).floatValue();
            }
            if (_o instanceof BigDecimal) {
                return ((BigDecimal) _o).floatValue();
            }
            if (_o instanceof Integer) {
                return ((Integer) _o).floatValue();
            }
            if (_o instanceof Short) {
                return ((Short) _o).floatValue();
            }
            if (_o instanceof Long) {
                return ((Long) _o).floatValue();
            }
            if (_o instanceof Boolean) {
                return ((Boolean) _o) ? 1.0f : 0.0f;
            }
            if (_o instanceof String) {
                return Float.valueOf((String) _o);
            }
            return 0.0f;
        }
        catch (Exception _e) {
            return 0.0f;
        }
    }

    public static int getInteger(DaoEntity _e, String _sField) {
        if (_e == null) {
            return 0;
        }
        return toInteger(_e.get(_sField));
    }

    public static int toInteger(Object _o) {
        try {
            if (_o instanceof Integer) {
                return (Integer) _o;
            }
            if (_o instanceof Short) {
                return ((Short) _o).intValue();
            }
            if (_o instanceof Long) {
                return ((Long) _o).intValue();
            }
            if (_o instanceof Double) {
                return ((Double) _o).intValue();
            }
            if (_o instanceof BigDecimal) {
                return ((BigDecimal) _o).intValue();
            }
            if (_o instanceof Boolean) {
                return ((Boolean) _o) ? 1 : 0;
            }
            if (_o instanceof String) {
                return Integer.valueOf((String) _o);
            }
            return 0;
        }
        catch (Exception _e) {
            return 0;
        }
    }

    public static long getLong(DaoEntity _e, String _sField) {
        if (_e == null) {
            return 0l;
        }
        return toLong(_e.get(_sField));
    }

    public static long toLong(Object _o) {
        try {
            if (_o instanceof Integer) {
                return ((Integer) _o).longValue();
            }
            if (_o instanceof Short) {
                return ((Short) _o).longValue();
            }
            if (_o instanceof Long) {
                return (Long) _o;
            }
            if (_o instanceof BigDecimal) {
                return ((BigDecimal) _o).longValue();
            }
            if (_o instanceof Double) {
                return ((Double) _o).longValue();
            }
            if (_o instanceof Boolean) {
                return ((Boolean) _o) ? 1L : 0L;
            }
            if (_o instanceof String) {
                return Long.valueOf((String) _o);
            }
            return 0L;
        }
        catch (Exception _e) {
            return 0L;
        }
    }

    public static boolean getBoolean(DaoEntity _e, String _field) {
        if (_e == null) {
            return false;
        }
        return toBoolean(_e.get(_field));
    }

    public static boolean toBoolean(Object _o) {
        return toBoolean(_o, false);
    }

    public static boolean toBoolean(Object _o, boolean _default) {
        if (_o instanceof Boolean)
            return (Boolean) _o;
        if (_o instanceof String)
            return ((String) _o).equalsIgnoreCase("true") || _o.equals("1");
        if (_o instanceof Integer)
            return ((Integer) _o) != 0;
        if (_o instanceof Long)
            return ((Long) _o) != 0;
        if (_o instanceof BigDecimal)
            return !(_o).equals(BigDecimal.ZERO);
        return _default;
    }

    public static byte[] getByteArray(DaoEntity _e, String _sField) {
        if (_e == null) {
            return null;
        }
        Object o = _e.get(_sField);
        if (o instanceof Binary)
            return ((Binary)o).getData();
        if (o instanceof byte[])
            return (byte[]) o;
        return null;
    }

    public static String toEnumName(Enum<?> _enum) {
        if (_enum == null)
            return "";
        return _enum.name();
    }

    public static <T extends Enum<T>> List<String> toEnumNames(Collection<T> _enums) {
        return CollectionUtils.transform(_enums, new ITransformer<T, String>() {
            @Override
            public String transform(T _enum) {
                return toEnumName(_enum);
            }
        });
    }

    public static <T extends Enum<T>> T getEnum(DaoEntity _e, String _sField, Class<T> _enumType) {
        return NullUtils.toEnum(_enumType, getString(_e, _sField));
    }

    public static <T extends Enum<T>> T getEnum(DaoEntity _e, String _sField, Class<T> _enumType, T _default) {
        return NullUtils.toEnum(_enumType, getString(_e, _sField), _default);
    }

    public static <T extends Enum<T>> List<T> toEnums(Collection<String> _enumNames, final Class<T> _enumType) {
        return CollectionUtils.transform(_enumNames, new ITransformer<String, T>() {
            @Override
            public T transform(String _s) {
                return NullUtils.toEnum(_enumType, _s);
            }
        });
    }

    public static DaoEntity getDaoEntity(DaoEntity _e, String _field) {
        if (_e == null)
            return null;
        return asDaoEntity(_e.get(_field));
    }

    public static DaoEntity asDaoEntity(Object _o) {
        if (_o instanceof Document)
            return new DaoEntity((Document) _o);
        if (_o instanceof DaoEntity)
            return (DaoEntity) _o;
        return null;
    }

    public static <T> T getObject(DaoEntity _e, String _field, Class<T> _class) {
        return getObject(_e, _field, _class, null);
    }

    public static <T> T getObject(DaoEntity _e, String _field, Class<T> _class, DaoProxyType _proxyType) {
        return fromDaoEntity(getDaoEntity(_e, _field), _class, _proxyType);
    }

    public static List<DaoEntity> toDaoEntities(Collection<? extends Object> _objects) {
        return toDaoEntities(_objects, null);
    }

    public static List<DaoEntity> toDaoEntities(Collection<? extends Object> _objects, DaoProxyType _proxyType) {
        List<DaoEntity> entities = new ArrayList<>(CollectionUtils.size(_objects));
        for (Object o : CollectionUtils.makeNotNull(_objects)) {
            entities.add(toDaoEntity(o, _proxyType));
        }
        return entities;
    }

    public static List<DaoEntity> getDaoEntityList(DaoEntity _d, String _field) {
        return getDaoEntityList(_d, _field, null);
    }

    public static List<DaoEntity> getDaoEntityList(DaoEntity _d, String _field, DaoProxyType _proxyType) {
        Object list = (_d == null) ? null : _d.get(_field);
        if (list instanceof Collection)
            return toDaoEntities((Collection<?>) list, _proxyType);
        return new ArrayList<>();
    }

    public static <T> List<T> getList(DaoEntity _d, String _sField, Class<T> _classOfT) {
        return getList(_d, _sField, _classOfT, null);
    }

    public static <T> List<T> getList(DaoEntity _d, String _sField, Class<T> _classOfT, DaoProxyType _proxyType) {
        if ((_d == null) || (!_d.containsKey(_sField)))
            return new ArrayList<T>();
        return fromList(_d.get(_sField), _classOfT, _proxyType);
    }

    public static <T> List<T> fromList(Object _list, Class<T> _classOfT) {
        return fromList(_list, _classOfT, null);
    }

    public static <T> List<T> fromList(Object _list, Class<T> _classOfT, DaoProxyType _proxyType) {
        if (_list instanceof List)
            return fromList((List<?>) _list, _classOfT, _proxyType);
        return new ArrayList<>();
    }

    public static <T> List<T> fromList(List<?> _list, Class<T> _classOfT) {
        return fromList(_list, _classOfT, null);
    }

    public static <T> List<T> fromList(List<?> _list, Class<T> _classOfT, DaoProxyType _proxyType) {
        List<T> objects = new ArrayList<>(CollectionUtils.size(_list));
        for (Object object : CollectionUtils.makeNotNull(_list)) {
            if (_classOfT.isInstance(object))
                objects.add(_classOfT.cast(object));
            else if (object instanceof Document)
                objects.add(fromDaoEntity(new DaoEntity((Document) object), _classOfT, _proxyType));
            else if (object instanceof DaoEntity)
                objects.add(fromDaoEntity((DaoEntity) object, _classOfT, _proxyType));
        }
        return objects;
    }

    public static List<Field> getSerializableFields(Class<?> _class) {
        return getSerializableFields(_class, false);
    }

    public static List<Field> getSerializableFields(Class<?> _class, boolean _serializeObjects) {
        List<Field> fields = new ArrayList<Field>();
        addSerializableFields(_class, fields, _serializeObjects);
        return fields;
    }

    private static void addSerializableFields(Class<?> _class, List<Field> _fields, boolean _serializeObjects) {
        if (_class == null) {
            return;
        }
        for (Field field : _class.getDeclaredFields()) {
            if (AbstractDaoSerializer.isSerializable(field, _serializeObjects))
                _fields.add(field);
        }
        addSerializableFields(_class.getSuperclass(), _fields, _serializeObjects);
    }

    public static Set<String> getIndexedFields(Class<?> _class) {
        return getIndexedFields(_class, null);
    }

    public static Set<String> getHashIndexFields(Class<?> _class) {
        return getIndexedFields(_class, true);
    }

    public static Set<String> getRangeIndexFields(Class<?> _class) {
        return getIndexedFields(_class, false);
    }

    private static Set<String> getIndexedFields(Class<?> _class, Boolean _hash) {
        Set<String> indexedFields = new HashSet<String>();
        DBSerializable def = _class.getAnnotation(DBSerializable.class);
        if (def != null) {
            for (DBIndex index : def.indexes()) {
                if ((_hash == null) || (_hash == index.hash())) {
                    Collections.addAll(indexedFields, index.columns());
                }
            }
        }
        return indexedFields;
    }

    public static boolean isAnnotationPresent(Class<?> _class, Class<? extends Annotation> _fieldAnnotation) {
        if (_class == null)
            return false;
        if (_class.isAnnotationPresent(_fieldAnnotation))
            return true;
        return isAnnotationPresent(_class.getSuperclass(), _fieldAnnotation);
    }

    public static <T extends Annotation> T getAnnotation(Class<?> _class, Class<T> _fieldAnnotation) {
        if (_class == null)
            return null;
        T a = _class.getAnnotation(_fieldAnnotation);
        if (a != null)
            return a;
        return getAnnotation(_class.getSuperclass(), _fieldAnnotation);
    }

    public static String toJson(Object _o) {
        return toJson(_o, true);
    }

    public static String toSingleLineJson(Object _o) {
        return toJson(toDaoEntity(_o), true, false);
    }

    public static String toSingleLineJson(DaoEntity _e) {
        return toJson(_e, true, false);
    }

    public static byte[] toZipBson(Object _o) {
        return toZipBson(toDaoEntity(_o));
    }

    public static byte[] toZipBson(DaoEntity _entity) {
        if (_entity == null)
            return null;
        return ZipUtils.zip(toBson(_entity, true));
    }

    public static <T> T fromZipBson(byte[] _btZipBson, Class<T> _class) {
        return DaoSerializer.fromDaoEntity(fromZipBson(_btZipBson), _class);
    }

    public static DaoEntity fromZipBson(byte[] _btZipBson) {
        return fromBson(ZipUtils.unzip(_btZipBson));
    }

    public static <T> T fromBson(byte[] _btBson, Class<T> _class) {
        return fromDaoEntity(fromBson(_btBson), _class);
    }

    public static DaoEntity fromBson(byte[] _btBson)
    {
        if (_btBson == null)
            return null;
        BsonBinaryReader reader = null;
        try
        {
            reader = new BsonBinaryReader(ByteBuffer.wrap(_btBson).order(ByteOrder.LITTLE_ENDIAN));
            Document doc = new DocumentCodec().decode(reader, DecoderContext.builder().build());
            if (doc == null)
                return null;
            return new DaoEntity(doc);
        }
        catch (Throwable t)
        {
            LOG.error("Failed to convert bson to DaoEntity", t);
            return null;
        }
        finally
        {
            if (reader != null)
                reader.close();
        }
    }

    public static byte[] toBson(Object _o) {
        return toBson(toDaoEntity(_o));
    }

    public static byte[] toBson(Object _o, boolean _removeNulls) {
        DaoEntity entity = toDaoEntity(_o);
        if (_removeNulls)
            removeNulls(entity.values());
        return toBson(entity);
    }

    public static byte[] toBson(DaoEntity _entity) {
        if (_entity == null)
            return null;
        BsonBinaryWriter writer = null;
        try
        {
            BasicOutputBuffer buffer = new BasicOutputBuffer();
            writer = new BsonBinaryWriter(buffer);
            new DocumentCodec().encode(writer, _entity.toDocument(), EncoderContext.builder().build());
            return buffer.toByteArray();
        }
        catch (Throwable t)
        {
            LOG.error("Failed to convert entity to BSON", t);
            return null;
        }
        finally
        {
            if (writer != null)
                writer.close();
        }
    }

    public static String toJson(Object _o, boolean _removeNulls) {
        return toJson(toDaoEntity(_o), _removeNulls);
    }

    public static String toJson(DaoEntity _e) {
        return toJson(_e, true);
    }

    public static String toJson(DaoEntity _e, boolean _removeNulls) {
        return toJson(_e, _removeNulls, true);
    }

    public static String toJson(DaoEntity _e, boolean _removeNulls, boolean _pretty) {
        try {
            if (_e != null) {
                Document doc = _e.toDocument();
                if (_removeNulls)
                    removeNulls(doc.values());
                JsonWriterSettings.Builder settings = JsonWriterSettings.builder().int64Converter(new Converter<Long>() {
                    @Override
                    public void convert(Long _long, StrictJsonWriter _writer) {
                        if (_long != null)
                            _writer.writeNumber(_long.toString());
                    }
                });
                return doc.toJson(settings.indent(_pretty).build());
            }
        }
        catch (Exception e) {
            LOG.error("Failed to convert DaoEntity to json", e);
        }
        return null;
    }

    private static void removeNulls(Collection<Object> _doc) {
        if (_doc == null)
            return;
        Iterator<Object> values = _doc.iterator();
        while (values.hasNext()) {
            Object o = values.next();
            if (o == null)
                values.remove();
            else if (o instanceof DaoEntity)
                removeNulls(((DaoEntity) o).values());
            else if (o instanceof Document)
                removeNulls(((Document) o).values());
            else if (o instanceof Collection) {
                Collection<Object> entities = (Collection<Object>) o;
                removeNulls(entities);
                if (entities.isEmpty())
                    values.remove();
            }
        }
    }

    public static String toJson(Collection<DaoEntity> _entities) {
        StringBuilder b = null;
        for (DaoEntity d : CollectionUtils.makeNotNull(_entities)) {
            if (b == null)
                b = new StringBuilder("[");
            else
                b.append(",");
            b.append(toJson(d));
        }
        if (b == null)
            return null;
        b.append("]");
        return b.toString();
    }

    public static <T> T parse(byte[] _json, Class<T> _class) {
        return fromDaoEntity(parse(NullUtils.toString(_json)), _class);
    }

    public static <T> T parse(String _json, Class<T> _class) {
        return fromDaoEntity(parse(_json), _class);
    }

    public static DaoEntity parse(String _json) {
        if (NullUtils.isEmpty(_json))
            return null;
        try {
            return new DaoEntity(Document.parse(_json));
        }
        catch (Exception _e) {
            LOG.error("Failed to parse json", _e);
            return null;
        }
    }

    public static <T> List<T> parseList(String _json, Class<T> _class) {
        return fromList(parseList(_json), _class);
    }

    public static List<DaoEntity> parseList(String _json) {
        try {
            List<DaoEntity> entities = new ArrayList<>();
            JsonReader bsonReader = new JsonReader(_json);
            for (Object o : new IterableCodec(CodecRegistries.fromProviders(Arrays.asList(new ValueCodecProvider(), new BsonValueCodecProvider(), new DocumentCodecProvider())), new BsonTypeClassMap()).decode(bsonReader, DecoderContext.builder().build())) {
                if (o instanceof Document)
                    entities.add(new DaoEntity((Document) o));
            }
            return entities;
        }
        catch (Exception _e) {
            LOG.error("Failed to parse json", _e);
            return null;
        }
    }

    public List<DaoSort> getIndexes(Class<?> _class) {
        IDaoSerializer<?> serializer = getSerializer(_class);
        return (serializer == null) ? new ArrayList<DaoSort>() : serializer.getIndexes();
    }
}
