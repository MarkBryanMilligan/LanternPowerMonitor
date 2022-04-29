package com.lanternsoftware.util.dao.ephemeral;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.IFilter;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.AbstractDaoProxy;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.DaoSort;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class EphemeralProxy extends AbstractDaoProxy {
    private static final Logger LOG = LoggerFactory.getLogger(EphemeralProxy.class);
    private final Map<String, Map<String, DaoEntity>> tables = new HashMap<>();
    private final Map<String, Class<?>> tableClasses = new HashMap<>();
    private final DaoProxyType serializerType;
    private long genericSequence = 100;

    public static EphemeralProxy loadFromDisk(String _path) {
        return loadFromDisk(_path, DaoProxyType.MONGO);
    }

    public static EphemeralProxy loadFromDisk(String _path, DaoProxyType _serializerType) {
        EphemeralProxy proxy = new EphemeralProxy(_serializerType);
        try {
            File file = new File(_path);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File child : files) {
                        if (child.getName().endsWith(".json")) {
                            Class<?> clazz;
                            try {
                                clazz = Class.forName(child.getName().substring(0, child.getName().length() - 5));
                            } catch (ClassNotFoundException _e) {
                                continue;
                            }
                            List<DaoEntity> entities = DaoSerializer.parseList(NullUtils.toString(ResourceLoader.loadFile(child)));
                            proxy.save(clazz, entities);
                        }
                    }
                }
            }
        } catch (Exception _e) {
            LOG.error("Failed to load directory: " + _path);
        }
        return proxy;
    }

    public EphemeralProxy() {
        this(DaoProxyType.MONGO);
    }

    public EphemeralProxy(DaoProxyType _serializerType) {
        serializerType = _serializerType;
    }

    public void writeToDisk(String _path) {
        writeToDisk(_path, null);
    }

    public void writeToDisk(String _path, String _fileNameSuffix) {
        File file = new File(_path);
        file.mkdirs();
        if (!_path.endsWith(File.separator))
            _path += File.separator;
        for (Entry<String, Map<String, DaoEntity>> e : tables.entrySet()) {
            try {
                String json = DaoSerializer.toJson(e.getValue().values());
                String filename = _path + tableClasses.get(e.getKey()).getCanonicalName();
                if (_fileNameSuffix != null)
                    filename += _fileNameSuffix;
                ResourceLoader.writeFile(filename, NullUtils.toByteArray(json));
            }
            catch (Throwable t) {
                LOG.error("Failed to write collection " + e.getKey() + " to disk", t);
            }
        }
    }

    @Override
    public DaoProxyType getType() {
        return serializerType;
    }

    @Override
    public synchronized List<DaoEntity> queryForEntities(String _tableName, DaoQuery _query, Collection<String> _fields, DaoSort _sort, int _offset, int _count) {
        Map<String, DaoEntity> table = tables.get(_tableName);
        if (table == null)
            return new ArrayList<>();
        return CollectionUtils.filter(table.values(), new QueryFilter(_query));
    }

    @Override
    public synchronized void update(Class<?> _class, DaoQuery _query, DaoEntity _changes) {
        for (DaoEntity entity : queryForEntities(DaoSerializer.getTableName(_class, getType()), _query)) {
            entity.putAll(_changes);
        }
    }

    @Override
    public synchronized <T> T updateOne(Class<T> _class, DaoQuery _query, DaoEntity _changes) {
        DaoEntity entity = CollectionUtils.getFirst(queryForEntities(DaoSerializer.getTableName(_class, getType()), _query));
        entity.putAll(_changes);
        return DaoSerializer.fromDaoEntity(entity, _class);
    }

    @Override
    public String saveEntity(String _collection, DaoEntity _entity) {
        return saveEntity(_collection, CollectionUtils.asArrayList("id"), _entity);
    }

    @Override
    public synchronized String saveEntity(Class<?> _class, DaoEntity _entity) {
        String tableName = DaoSerializer.getTableName(_class, getType());
        tableClasses.put(tableName, _class);
        return saveEntity(tableName, DaoSerializer.getFieldsByAnnotation(_class, PrimaryKey.class), _entity);
    }

    private String saveEntity(String _tableName, List<String> _primaryKeys, DaoEntity _entity) {
        if (_entity == null)
            return null;
        String pk;
        if (!_entity.containsKey("_id")) {
            for (String key : CollectionUtils.makeNotNull(_primaryKeys)) {
                Object value = _entity.remove(key);
                if ((value instanceof String) || (value == null)) {
                    if (NullUtils.isEmpty((String) value)) {
                        value = UUID.randomUUID().toString();
                    }
                } else if (value instanceof Long) {
                    if (((Long) value) == 0) {
                        value = getNextSequence();
                    }
                } else if (value instanceof Integer) {
                    if (((Integer) value) == 0) {
                        value = Long.valueOf(getNextSequence()).intValue();
                    }
                }
                _entity.put(key, value);
            }
            pk = CollectionUtils.commaSeparated(CollectionUtils.transform(CollectionUtils.getAll(_entity, _primaryKeys), DaoSerializer::toString));
        }
        else
            pk = DaoSerializer.getString(_entity, "_id");
        Map<String, DaoEntity> table = tables.get(_tableName);
        if (table != null)
            table.remove(pk);
        else {
            table = new HashMap<>();
            tables.put(_tableName, table);
        }
        table.put(pk, _entity);
        return pk;
    }

    private long getNextSequence() {
        return genericSequence++;
    }

    @Override
    public synchronized boolean delete(String _tableName, DaoQuery _query) {
        IFilter<DaoEntity> filter = new QueryFilter(_query);
        Map<String, DaoEntity> table = tables.get(_tableName);
        if (table != null) {
            table.values().removeIf(filter::isFiltered);
        }
        return true;
    }

    @Override
    public int count(String _tableName, DaoQuery _query) {
        return queryForEntities(_tableName, _query).size();
    }

    private class QueryFilter implements IFilter<DaoEntity> {
        private final DaoQuery query;

        QueryFilter(DaoQuery _query) {
            query = _query;
        }

        @Override
        public boolean isFiltered(DaoEntity _daoEntity) {
            if (query == null)
                return true;
            for (Entry<String, Object> qual : query.entrySet()) {
                if (qual.getValue() instanceof DaoQuery) {
                    DaoQuery child = (DaoQuery) qual.getValue();
                    Object comp = child.get("$ne");
                    if ((comp != null) && DaoSerializer.compare(_daoEntity, qual.getKey(), comp) == 0)
                        return false;
                    comp = child.get("$gt");
                    if ((comp != null) && DaoSerializer.compare(_daoEntity, qual.getKey(), comp) <= 0)
                        return false;
                    comp = child.get("$lt");
                    if ((comp != null) && DaoSerializer.compare(_daoEntity, qual.getKey(), comp) >= 0)
                        return false;
                    comp = child.get("$gte");
                    if ((comp != null) && DaoSerializer.compare(_daoEntity, qual.getKey(), comp) < 0)
                        return false;
                    comp = child.get("$lte");
                    if ((comp != null) && DaoSerializer.compare(_daoEntity, qual.getKey(), comp) > 0)
                        return false;
                    comp = child.get("$contains");
                    if ((comp != null) && !DaoSerializer.getString(_daoEntity, qual.getKey()).contains((String) comp))
                        return false;
                    comp = child.get("$startsWith");
                    if ((comp != null) && !DaoSerializer.getString(_daoEntity, qual.getKey()).startsWith((String) comp))
                        return false;
                    comp = child.get("$containsIgnoreCase");
                    if ((comp != null) && !DaoSerializer.getString(_daoEntity, qual.getKey()).toLowerCase().contains(((String) comp).toLowerCase()))
                        return false;
                    comp = child.get("$equalssIgnoreCase");
                    if ((comp != null) && !DaoSerializer.getString(_daoEntity, qual.getKey()).equalsIgnoreCase(((String) comp)))
                        return false;
                    comp = child.get("$startsWithIgnoreCase");
                    if ((comp != null) && !DaoSerializer.getString(_daoEntity, qual.getKey()).toLowerCase().startsWith(((String) comp).toLowerCase()))
                        return false;
                    comp = child.get("$in");
                    if ((comp != null) && !in(_daoEntity.get(qual.getKey()), (Collection<?>) comp))
                        return false;
                    comp = child.get("$nin");
                    if ((comp != null) && in(_daoEntity.get(qual.getKey()), (Collection<?>) comp))
                        return false;
                }
                else if ((qual.getValue() instanceof String) && NullUtils.isEqual(qual.getValue(), "$null")) {
                    if (_daoEntity.get(qual.getKey()) != null)
                        return false;
                }
                else if ((qual.getValue() instanceof String) && NullUtils.isEqual(qual.getValue(), "$notnull")) {
                    if (_daoEntity.get(qual.getKey()) == null)
                        return false;
                }
                else if (DaoSerializer.compare(_daoEntity, qual.getKey(), qual.getValue()) != 0)
                    return false;
            }
            return true;
        }
    }

    private boolean in(Object field, Collection<?> qual) {
        for (Object qualObject : qual) {
            if (field instanceof Collection) {
                for (Object fieldObject : (Collection<?>) field) {
                    if (NullUtils.isEqual(fieldObject, qualObject))
                        return true;
                }
            }
            else if (NullUtils.isEqual(field, qualObject))
                return true;
        }
        return false;
    }
}
