package com.lanternsoftware.util.dao.mongo;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.ITransformer;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.cryptography.RSAUtils;
import com.lanternsoftware.util.dao.AbstractDaoProxy;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.DaoSort;
import com.lanternsoftware.util.dao.DaoSortField;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;
import com.lanternsoftware.util.hash.MD5HashTool;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class MongoProxy extends AbstractDaoProxy {
    private static final Logger LOG = LoggerFactory.getLogger(MongoProxy.class);
    private final MongoClient client;
    private final String dbName;
    private final Map<String, Set<String>> textIndexes = new HashMap<>();
    private final MD5HashTool hash = new MD5HashTool();

    public MongoProxy(MongoConfig _config) {
        this(_config.getHosts(), _config.getUsername(), _config.getPassword(), _config.getClientKeystorePath(), _config.getClientKeystorePassword(), _config.getCaKeystorePath(), _config.getCaKeystorePassword(), _config.getDatabaseName(), _config.getAuthenticationDatabase());
    }

    public MongoProxy(List<String> _hosts, String _userName, String _password, String _clientKeystorePath, String _clientKeystorePassword, String _caKeystorePath, String _caKeystorePassword, String _dbName) {
        this(_hosts, _userName, _password, _clientKeystorePath, _clientKeystorePassword, _caKeystorePath, _caKeystorePassword, _dbName, null);
    }

    public MongoProxy(List<String> _hosts, String _userName, String _password, String _clientKeystorePath, String _clientKeystorePassword, String _caKeystorePath, String _caKeystorePassword, String _dbName, String _authDbName) {
        List<ServerAddress> listAddresses = new LinkedList<>();
        if (CollectionUtils.isEmpty(_hosts))
            _hosts = CollectionUtils.asArrayList("localhost");
        for (String addr : _hosts) {
            int portIdx = addr.indexOf(":");
            if (portIdx > 0)
                listAddresses.add(new ServerAddress(addr.substring(0, portIdx), NullUtils.toInteger(addr.substring(portIdx + 1))));
            else
                listAddresses.add(new ServerAddress(addr, 27017));
        }
        MongoClientSettings.Builder settings = MongoClientSettings.builder();
        if (NullUtils.isEmpty(_clientKeystorePath) && NullUtils.isEmpty(_caKeystorePath))
            settings.applyToSslSettings(builder -> builder.enabled(false)).build();
        else if (NullUtils.isEqual(_caKeystorePath, "default"))
            settings.applyToSslSettings(builder -> builder.enabled(true)).build();
        else {
            try {
                KeyManager[] keyManagers = null;
                if (NullUtils.isNotEmpty(_clientKeystorePath)) {
                    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
                    kmf.init(RSAUtils.loadKeystore(_clientKeystorePath, _clientKeystorePassword), _clientKeystorePassword.toCharArray());
                    keyManagers = kmf.getKeyManagers();
                }
                TrustManager[] trustManagers = null;
                if (NullUtils.isNotEmpty(_caKeystorePath)) {
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
                    tmf.init(RSAUtils.loadKeystore(_caKeystorePath, _caKeystorePassword));
                    trustManagers = tmf.getTrustManagers();
                }
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(keyManagers, trustManagers, null);
                settings.applyToSslSettings(builder -> {
                    builder.enabled(true);
                    builder.context(sslContext);
                }).build();
            }
            catch (Exception _e) {
                LOG.error("Failed to load keystores for MongoClient", _e);
                settings.applyToSslSettings(builder -> builder.enabled(false)).build();
            }
        }
        if (NullUtils.isNotEmpty(_userName))
            settings.credential(MongoCredential.createCredential(_userName, NullUtils.isNotEmpty(_authDbName) ? _authDbName : "admin", _password.toCharArray()));
        settings.applyToClusterSettings(_b->_b.hosts(listAddresses));
        settings.retryWrites(false);
        settings.retryReads(false);
        client = MongoClients.create(settings.build());
        dbName = _dbName;
    }

    @Override
    public DaoProxyType getType() {
        return DaoProxyType.MONGO;
    }

    @Override
    public void shutdown() {
        client.close();
    }

    @Override
    public <T> List<T> query(final Class<T> _class, DaoQuery _query, Collection<String> _fields, DaoSort _sort, int _first, int _count) {
        return toObjects(queryForEntities(DaoSerializer.getTableName(_class, getType()), CollectionUtils.getFirst(DaoSerializer.getFieldsByAnnotation(_class, PrimaryKey.class)), _query, _fields, _sort, _first, _count), _class);
    }

    @Override
    public List<String> queryForField(Class<?> _class, DaoQuery _query, final String _field) {
        String pk = CollectionUtils.getFirst(DaoSerializer.getFieldsByAnnotation(_class, PrimaryKey.class));
        return CollectionUtils.transform(queryForEntities(DaoSerializer.getTableName(_class, getType()), pk, _query, Collections.singletonList(_field), null, 0, -1), new ITransformer<DaoEntity, String>() {
            @Override
            public String transform(DaoEntity _daoEntity) {
                return DaoSerializer.getString(_daoEntity, _field);
            }
        });
    }

    @Override
    public List<DaoEntity> queryForEntities(String _tableName, DaoQuery _query, Collection<String> _fields, DaoSort _sort, int _offset, int _count) {
        return queryForEntities(_tableName, null, _query, _fields, _sort, _offset, _count);
    }

    public List<DaoEntity> queryForEntities(String _tableName, final String _primaryKey, DaoQuery _query, Collection<String> _fields, DaoSort _sort, int _offset, int _count) {
        return CollectionUtils.transform(queryIterator(_tableName, _primaryKey, _query, _fields, _sort, _offset, _count), DaoEntity::new);
    }

    public <T> Iterable<T> queryIterator(Class<T> _class, DaoQuery _query, Collection<String> _fields, DaoSort _sort, int _offset, int _count) {
        String pk = CollectionUtils.getFirst(DaoSerializer.getFieldsByAnnotation(_class, PrimaryKey.class));
        return queryIterator(DaoSerializer.getTableName(_class, getType()), pk, _query, _fields, _sort, _offset, _count).map(_d->DaoSerializer.fromDaoEntity(new DaoEntity(_d), _class));
    }

    public FindIterable<Document> queryIterator(String _tableName, final String _primaryKey, DaoQuery _query, Collection<String> _fields, DaoSort _sort, int _offset, int _count) {
        final String pk = NullUtils.isEmpty(_primaryKey) ? "_id" : _primaryKey;
        FindIterable<Document> iter;
        if (_query != null) {
            DaoQuery query = new DaoQuery();
            for (Entry<String, Object> entry : _query.entrySet()) {
                if (NullUtils.isEqual(entry.getKey(), pk))
                    query.put("_id", entry.getValue());
                else {
                    boolean keyChanged = false;
                    if (entry.getValue() instanceof DaoQuery) {
                        DaoQuery child = (DaoQuery) entry.getValue();
                        Map<String, Object> newChildren = new HashMap<>();
                        Iterator<Entry<String, Object>> entryIter = child.entrySet().iterator();
                        while (entryIter.hasNext()) {
                            Entry<String, Object> childEntry = entryIter.next();
                            if (childEntry.getKey().startsWith("$contains")) {
                                boolean caseSensitive = !NullUtils.isEqual(childEntry.getKey(), "$containsIgnoreCase");
                                if (isTextIndex(_tableName, entry.getKey())) {
                                    DaoQuery value = new DaoQuery();
                                    value.put("$search", childEntry.getValue());
                                    value.put("$caseSensitive", caseSensitive);
                                    value.put("$diacriticSensitive", caseSensitive);
                                    query.put("$text", value);
                                    entryIter.remove();
                                    keyChanged = true;
                                    break;
                                }
                                else {
                                    if (childEntry.getValue() instanceof String) {
                                        newChildren.put("$regex", childEntry.getValue());
                                        if (!caseSensitive)
                                            newChildren.put("$options", "i");
                                    }
                                    entryIter.remove();
                                }
                            }
                        }
                        child.putAll(newChildren);
                    }
                    if (!keyChanged)
                        query.put(entry.getKey(), entry.getValue());
                }
            }
            query = prepareQuery(query);
            iter = db().getCollection(_tableName).find(query);
        }
        else
            iter = db().getCollection(_tableName).find();
        if (_fields != null) {
            List<String> fields = new ArrayList<>();
            for (String field : _fields) {
                if (NullUtils.isEqual(field, pk))
                    fields.add("_id");
                else
                    fields.add(field);
            }
            _fields = fields;
        }
        Document projection = toProjection(_fields);
        if (projection != null)
            iter.projection(projection);
        Document sort = toSort(_sort);
        if (sort != null)
            iter.sort(sort);
        if (_offset > 0)
            iter.skip(_offset);
        if (_count > 0)
            iter.limit(_count);
        return iter;
    }

    @Override
    public void update(Class<?> _class, DaoQuery _query, DaoEntity _changes) {
        DaoQuery query = prepareQuery(_query);
        coll(_class).updateMany(query, _changes.toDocument());
    }

    @Override
    public <T> T updateOne(Class<T> _class, DaoQuery _query, DaoEntity _changes) {
        return DaoSerializer.fromDaoEntity(new DaoEntity(coll(_class).findOneAndUpdate(_query, _changes.toDocument())), _class);
    }

    @Override
    public String saveEntity(Class<?> _class, DaoEntity _entity) {
        if (_entity == null)
            return null;
        String id = DaoSerializer.getString(_entity, "_id");
        if (NullUtils.isEmpty(id)) {
            String primaryKeyField = CollectionUtils.getFirst(DaoSerializer.getFieldsByAnnotation(_class, PrimaryKey.class));
            if (NullUtils.isEmpty(primaryKeyField)) {
                primaryKeyField = "_id";
            }
            id = (String) _entity.remove(primaryKeyField);
            if (NullUtils.isEmpty(id))
                id = UUID.randomUUID().toString();
            _entity.put("_id", id);
        }
        Document doc = _entity.toDocument();
        UpdateResult result = coll(_class).replaceOne(new Document("_id", id), doc, new ReplaceOptions().upsert(true));
        if (result.wasAcknowledged())
            return id;
        return null;
    }

    public String saveEntity(String _tableName, DaoEntity _entity) {
        String id = DaoSerializer.getString(_entity, "_id");
        if (NullUtils.isEmpty(id)) {
            id = UUID.randomUUID().toString();
            _entity.put("_id", id);
        }
        Document doc = _entity.toDocument();
        UpdateResult result = db().getCollection(_tableName).replaceOne(new Document("_id", id), doc, new ReplaceOptions().upsert(true));
        if (result.wasAcknowledged())
            return id;
        return null;
    }

    @Override
    public <T> Map<String, T> save(Collection<T> _objects) {
        if (CollectionUtils.isEmpty(_objects))
            return new HashMap<>();
        Iterator<T> iter = _objects.iterator();
        while (iter.hasNext()) {
            T t = iter.next();
            if (t == null)
                iter.remove();
        }
        T t = CollectionUtils.getFirst(_objects);
        if (t == null)
            return new HashMap<>();
        Map<Class<?>, List<T>> classes = CollectionUtils.transformToMultiMap(_objects, new ITransformer<T, Class<?>>() {
            @Override
            public Class<?> transform(T _t) {
                return _t.getClass();
            }
        });
        final Map<String, T> ids = new HashMap<>();
        for (Entry<Class<?>, List<T>> e : classes.entrySet()) {
            String primaryKeyField = CollectionUtils.getFirst(DaoSerializer.getFieldsByAnnotation(e.getKey(), PrimaryKey.class));
            if (NullUtils.isEmpty(primaryKeyField)) {
                primaryKeyField = "_id";
            }
            final String pk = primaryKeyField;
            for (Collection<T> entities : CollectionUtils.split(e.getValue(), 5000)) {
                List<WriteModel<Document>> updates = CollectionUtils.transform(entities, (_t) -> {
                    DaoEntity entity = DaoSerializer.toDaoEntity(_t, getType());
                    if (entity == null)
                        return null;
                    String id = DaoSerializer.getString(entity, "_id");
                    if (NullUtils.isEmpty(id)) {
                        id = (String) entity.remove(pk);
                        if (NullUtils.isEmpty(id))
                            id = UUID.randomUUID().toString();
                        entity.put("_id", id);
                    }
                    ids.put(id, _t);
                    return new ReplaceOneModel<>(new Document("_id", id), entity.toDocument(), new ReplaceOptions().upsert(true));
                }, true);
                if (!updates.isEmpty())
                    coll(e.getKey()).bulkWrite(updates);
            }
        }
        return ids;
    }

    @Override
    public Map<String, DaoEntity> save(Class<?> _class, Collection<DaoEntity> _entities) {
        String primaryKeyField = CollectionUtils.getFirst(DaoSerializer.getFieldsByAnnotation(_class, PrimaryKey.class));
        if (NullUtils.isEmpty(primaryKeyField)) {
            primaryKeyField = "_id";
        }
        final Map<String, DaoEntity> ids = new HashMap<>();
        final String pk = primaryKeyField;
        List<DaoEntity> entities = (_entities instanceof List) ? (List<DaoEntity>) _entities : new ArrayList<>(_entities);
        for (Collection<DaoEntity> curEntities : CollectionUtils.split(entities, 5000)) {
            List<WriteModel<Document>> updates = CollectionUtils.transform(curEntities, (_t) -> {
                String id = (String) _t.remove(pk);
                if (NullUtils.isEmpty(id))
                    id = UUID.randomUUID().toString();
                _t.put("_id", id);
                ids.put(id, _t);
                return new ReplaceOneModel<>(new Document("_id", id), _t.toDocument(), new ReplaceOptions().upsert(true));
            }, true);
            if (!updates.isEmpty())
                coll(_class).bulkWrite(updates);
        }
        return ids;
    }

    public <T> T queryOneAndDelete(final Class<T> _class, DaoQuery _query) {
        Document doc = coll(_class).findOneAndDelete(_query);
        if (doc == null)
            return null;
        return toObject(new DaoEntity(doc), _class);
    }

    public int deleteById(Class<?> _class, List<String> _ids) {
        BulkWriteResult result = coll(_class).bulkWrite(CollectionUtils.transform(_ids, _t -> new DeleteOneModel<>(new Document("_id", _t))));
        if (result.wasAcknowledged())
            return result.getDeletedCount();
        return 0;
    }

    @Override
    public boolean delete(Class<?> _class, DaoQuery _query) {
        if (_query != null) {
            String primaryKey = CollectionUtils.getFirst(DaoSerializer.getFieldsByAnnotation(_class, PrimaryKey.class));
            DaoQuery query = new DaoQuery();
            for (Entry<String, Object> entry : _query.entrySet()) {
                if (NullUtils.isEqual(entry.getKey(), primaryKey))
                    query.put("_id", entry.getValue());
                else
                    query.put(entry.getKey(), entry.getValue());
            }
            return delete(DaoSerializer.getTableName(_class, getType()), query);
        }
        return false;
    }

    @Override
    public boolean delete(String _tableName, DaoQuery _query) {
        DeleteResult result = db().getCollection(_tableName).deleteMany(prepareQuery(_query));
        return result.wasAcknowledged();
    }

    @Override
    public int count(String _tableName, DaoQuery _query) {
        if (CollectionUtils.isEmpty(_query))
            return (int) db().getCollection(_tableName).countDocuments();
        else
            return (int) db().getCollection(_tableName).countDocuments(prepareQuery(_query));
    }

    public void ensureIndex(Class<?> _class, DaoSort _indexOrder) {
        Document index = new Document();
        for (DaoSortField field : _indexOrder.getFields()) {
            index.put(field.getField(), field.isAscending() ? 1 : -1);
        }
        String indexName = CollectionUtils.transformAndDelimit(_indexOrder.getFields(), new ITransformer<DaoSortField, String>() {
            @Override
            public String transform(DaoSortField _daoSortField) {
                return _daoSortField.getField();
            }
        }, "_");
        LOG.debug("Ensuring index: " + indexName);
        String tableName = NullUtils.makeNotNull(DaoSerializer.getTableName(_class, getType()));
        if ((tableName.length() + indexName.length()) > 60) {
            indexName = hash.hash64(indexName);
            LOG.debug("Shortening index name to : " + indexName);
        }
        IndexOptions options = new IndexOptions();
        options.name(indexName);
        options.background(true);
        if (!index.isEmpty())
            db().getCollection(tableName).createIndex(index, options);
    }

    private MongoCollection<Document> coll(Class<?> _class) {
        return db().getCollection(DaoSerializer.getTableName(_class, getType()));
    }

    private MongoDatabase db() {
        return client.getDatabase(dbName);
    }

    private Document toProjection(Collection<String> _listFields) {
        if (CollectionUtils.isEmpty(_listFields))
            return null;
        Document proj = new Document();
        for (String field : CollectionUtils.makeNotNull(_listFields)) {
            proj.put(field, 1);
        }
        return proj;
    }

    private Document toSort(DaoSort _sort) {
        if ((_sort == null) || CollectionUtils.isEmpty(_sort.getFields()))
            return null;
        Document sort = new Document();
        for (DaoSortField field : CollectionUtils.makeNotNull(_sort.getFields())) {
            sort.put(field.getField(), field.isAscending() ? 1 : -1);
        }
        return sort;
    }

    private boolean isTextIndex(String _collection, String _field) {
        Set<String> fields = textIndexes.get(_collection);
        if (fields == null) {
            fields = new HashSet<>();
            for (Document index : db().getCollection(_collection).listIndexes()) {
                for (Entry<String, Object> field : index.entrySet()) {
                    if (field.getValue() instanceof String && field.getValue().equals("text"))
                        fields.add(field.getKey());
                }
            }
            textIndexes.put(_collection, fields);
        }
        return fields.contains(_field);
    }

    @Override
    protected DaoQuery prepareQuery(DaoQuery _query) {
        DaoQuery query = super.prepareQuery(_query);
        prepareDates(query);
        return query;
    }

    private void prepareDates(DaoQuery _query) {
        for (Entry<String, Object> e : _query.entrySet()) {
            if (e.getValue() instanceof Date)
                e.setValue(((Date) e.getValue()).getTime());
            if (e.getValue() instanceof DaoQuery)
                prepareDates((DaoQuery) e.getValue());
        }
    }
}