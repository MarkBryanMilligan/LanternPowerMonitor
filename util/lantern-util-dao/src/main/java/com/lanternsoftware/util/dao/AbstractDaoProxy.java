package com.lanternsoftware.util.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.ITransformer;
import com.lanternsoftware.util.NullUtils;

public abstract class AbstractDaoProxy implements IDaoProxy {
    private ExecutorService executor;
    private int maxThreads = 50;
    protected QueryPreparer queryPreparer = null;

    @Override
    public void shutdown() {
        if (executor != null)
            executor.shutdownNow();
    }

    public void setQueryPreparer(QueryPreparer _queryPreparer) {
        queryPreparer = _queryPreparer;
    }

    @Override
    public <T> List<T> query(Class<T> _class, DaoQuery _query) {
        return query(_class, _query, (DaoSort) null);
    }

    @Override
    public <T> List<T> query(final Class<T> _class, DaoQuery _query, DaoSort _sort) {
        return query(_class, _query, null, _sort);
    }

    @Override
    public <T> List<T> query(Class<T> _class, DaoQuery _query, Collection<String> _fields) {
        return query(_class, _query, _fields, null);
    }

    @Override
    public <T> List<T> query(final Class<T> _class, DaoQuery _query, Collection<String> _fields, DaoSort _sort) {
        return query(_class, _query, _fields, _sort, 0, -1);
    }

    @Override
    public <T> List<T> query(final Class<T> _class, DaoQuery _query, Collection<String> _fields, DaoSort _sort, int _first, int _count) {
        return toObjects(queryForEntities(DaoSerializer.getTableName(_class, getType()), _query, _fields, _sort, _first, _count), _class);
    }

    @Override
    public <T> Future<List<T>> queryAsync(Class<T> _class, DaoQuery _query) {
        return submit(new QueryExecution<T>(this, _class, _query));
    }

    @Override
    public <T> Future<List<T>> queryAsync(Class<T> _class, DaoQuery _query, DaoSort _sort) {
        return submit(new QueryExecution<T>(this, _class, _query, _sort));
    }

    @Override
    public <T> Future<List<T>> queryAsync(Class<T> _class, DaoQuery _query, Collection<String> _fields) {
        return submit(new QueryExecution<T>(this, _class, _query, _fields));
    }

    @Override
    public <T> Future<List<T>> queryAsync(Class<T> _class, DaoQuery _query, Collection<String> _fields, DaoSort _sort) {
        return submit(new QueryExecution<T>(this, _class, _query, _fields, _sort));
    }

    @Override
    public <T, V> Future<List<V>> queryWithFinalizer(Class<T> _class, DaoQuery _query, Collection<String> _fields, DaoSort _sort, QueryFinalizer<T, V> _finalizer) {
        return submit(new QueryFinalizerExecution<T, V>(this, _class, _query, _fields, _sort, _finalizer));
    }

    @Override
    public <T> DaoPage<T> queryPage(Class<T> _class, DaoQuery _query, Collection<String> _fields, DaoSort _sort, int _offset, int _count) {
        return new DaoPage<T>(query(_class, _query, _fields, _sort, _offset, _count), count(_class, _query));
    }

    @Override
    public DaoPage<DaoEntity> queryForEntitiesPage(String _tableName, DaoQuery _query, Collection<String> _fields, DaoSort _sort, int _offset, int _count) {
        return new DaoPage<DaoEntity>(queryForEntities(_tableName, _query, _fields, _sort, _offset, _count), count(_tableName, _query));
    }

    @Override
    public <T> T queryOne(Class<T> _class, DaoQuery _query) {
        return queryOne(_class, _query, null, null);
    }

    @Override
    public <T> T queryOne(Class<T> _class, DaoQuery _query, DaoSort _sort) {
        return queryOne(_class, _query, null, _sort);
    }

    @Override
    public <T> T queryOne(Class<T> _class, DaoQuery _query, Collection<String> _fields) {
        return queryOne(_class, _query, _fields, null);
    }

    @Override
    public <T> T queryOne(Class<T> _class, DaoQuery _query, Collection<String> _fields, DaoSort _sort) {
        return CollectionUtils.getFirst(query(_class, _query, _fields, _sort, 0, 1));
    }

    @Override
    public <T> Future<T> queryOneAsync(Class<T> _class, DaoQuery _query) {
        return submit(new QueryOneExecution<T>(this, _class, _query));
    }

    @Override
    public <T> Future<T> queryOneAsync(Class<T> _class, DaoQuery _query, DaoSort _sort) {
        return submit(new QueryOneExecution<T>(this, _class, _query, _sort));
    }

    @Override
    public <T> Future<T> queryOneAsync(Class<T> _class, DaoQuery _query, Collection<String> _fields) {
        return submit(new QueryOneExecution<T>(this, _class, _query, _fields));
    }

    @Override
    public <T> Future<T> queryOneAsync(Class<T> _class, DaoQuery _query, Collection<String> _fields, DaoSort _sort) {
        return submit(new QueryOneExecution<T>(this, _class, _query, _fields, _sort));
    }

    @Override
    public <T> List<T> queryImportant(Class<T> _class, DaoQuery _query) {
        return queryImportant(_class, _query, null);
    }

    @Override
    public <T> List<T> queryImportant(Class<T> _class, DaoQuery _query, DaoSort _sort) {
        return query(_class, _query, DaoSerializer.getImportantFields(_class), _sort);
    }

    @Override
    public <T> List<T> queryImportant(Class<T> _class, DaoQuery _query, DaoSort _sort, int _first, int _count) {
        return query(_class, _query, DaoSerializer.getImportantFields(_class), _sort, _first, _count);
    }

    @Override
    public <T> Future<List<T>> queryImportantAsync(Class<T> _class, DaoQuery _query) {
        return queryAsync(_class, _query, DaoSerializer.getImportantFields(_class));
    }

    @Override
    public <T> Future<List<T>> queryImportantAsync(Class<T> _class, DaoQuery _query, DaoSort _sort) {
        return queryAsync(_class, _query, DaoSerializer.getImportantFields(_class), _sort);
    }

    @Override
    public <T> DaoPage<T> queryImportantPage(Class<T> _class, DaoQuery _query, DaoSort _sort, int _offset, int _count) {
        return new DaoPage<T>(queryImportant(_class, _query, _sort, _offset, _count), count(_class, _query));
    }

    @Override
    public <T> List<T> queryAll(Class<T> _class) {
        return query(_class, null);
    }

    @Override
    public boolean exists(Class<?> _class, DaoQuery _query) {
        return exists(DaoSerializer.getTableName(_class, getType()), _query);
    }

    @Override
    public boolean exists(String _tableName, DaoQuery _query) {
        return count(_tableName, _query) > 0;
    }

    @Override
    public List<DaoEntity> queryForEntities(String _tableName, DaoQuery _query) {
        return queryForEntities(_tableName, _query, (DaoSort) null);
    }

    @Override
    public List<DaoEntity> queryForEntities(String _tableName, DaoQuery _query, Collection<String> _fields) {
        return queryForEntities(_tableName, _query, _fields, null);
    }

    @Override
    public List<DaoEntity> queryForEntities(String _tableName, DaoQuery _query, DaoSort _sort) {
        return queryForEntities(_tableName, _query, null, _sort);
    }

    @Override
    public List<DaoEntity> queryForEntities(String _tableName, DaoQuery _query, Collection<String> _fields, DaoSort _sort) {
        return queryForEntities(_tableName, _query, _fields, _sort, 0, -1);
    }

    @Override
    public DaoEntity queryForEntity(String _tableName, DaoQuery _query) {
        return CollectionUtils.getFirst(queryForEntities(_tableName, _query, null, null, 0, 1));
    }

    @Override
    public DaoEntity queryForEntity(String _tableName, DaoQuery _query, DaoSort _sort) {
        return CollectionUtils.getFirst(queryForEntities(_tableName, _query, null, _sort, 0, 1));
    }

    @Override
    public DaoEntity queryForEntity(String _tableName, DaoQuery _query, Collection<String> _fields) {
        return CollectionUtils.getFirst(queryForEntities(_tableName, _query, _fields, null, 0, 1));
    }

    @Override
    public DaoEntity queryForEntity(String _tableName, DaoQuery _query, Collection<String> _fields, DaoSort _sort) {
        return CollectionUtils.getFirst(queryForEntities(_tableName, _query, _fields, _sort, 0, 1));
    }

    @Override
    public String queryForOneField(Class<?> _class, DaoQuery _query, String _field) {
        return CollectionUtils.getFirst(queryForField(_class, _query, _field));
    }

    @Override
    public List<String> queryForField(Class<?> _class, DaoQuery _query, String _field) {
        return queryForField(DaoSerializer.getTableName(_class, getType()), _query, _field);
    }

    @Override
    public List<String> queryForField(Class<?> _class, DaoQuery _query, final String _field, DaoSort _sort) {
        return CollectionUtils.transform(queryForEntities(DaoSerializer.getTableName(_class, getType()), _query, Arrays.asList(_field), _sort), new ITransformer<DaoEntity, String>() {
            @Override
            public String transform(DaoEntity _daoEntity) {
                return DaoSerializer.getString(_daoEntity, _field);
            }
        });
    }

    @Override
    public List<String> queryForField(String _tableName, DaoQuery _query, final String _field) {
        return CollectionUtils.transform(queryForEntities(_tableName, _query, Arrays.asList(_field)), new ITransformer<DaoEntity, String>() {
            @Override
            public String transform(DaoEntity _daoEntity) {
                return DaoSerializer.getString(_daoEntity, _field);
            }
        });
    }

    @Override
    public String save(Object _object) {
        return saveEntity(_object.getClass(), DaoSerializer.toDaoEntity(_object, getType()));
    }

    @Override
    public <T> Map<String, T> save(Collection<T> _objects) {
        Map<String, T> ids = new HashMap<String, T>();
        for (T o : _objects) {
            String id = save(o);
            if (NullUtils.isNotEmpty(id))
                ids.put(id, o);
        }
        return ids;
    }

    @Override
    public Map<String, DaoEntity> save(Class<?> _class, Collection<DaoEntity> _entities) {
        Map<String, DaoEntity> ids = new HashMap<>();
        for (DaoEntity e : _entities) {
            ids.put(saveEntity(_class, e), e);
        }
        return ids;
    }

    @Override
    public boolean delete(Class<?> _class, DaoQuery _query) {
        return delete(DaoSerializer.getTableName(_class, getType()), _query);
    }

    @Override
    public int count(Class<?> _class, DaoQuery _query) {
        return count(DaoSerializer.getTableName(_class, getType()), _query);
    }

    private <T> Future<List<T>> submit(Callable<List<T>> _execution) {
        return executor().submit(_execution);
    }

    private <T> Future<T> submit(QueryOneExecution<T> _execution) {
        return executor().submit(_execution);
    }

    public void setMaxThreads(int _maxThreads) {
        maxThreads = _maxThreads;
    }

    @Override
    public void setExecutor(ExecutorService _executor) {
        executor = _executor;
    }

    private synchronized ExecutorService executor() {
        if (executor == null)
            executor = Executors.newFixedThreadPool(maxThreads);
        return executor;
    }

    protected <T> T toObject(DaoEntity _entity, Class<T> _class) {
        return CollectionUtils.getFirst(toObjects(Collections.singletonList(_entity), _class));
    }

    protected <T> List<T> toObjects(List<DaoEntity> _entities, final Class<T> _class) {
        return CollectionUtils.transform(_entities, new ITransformer<DaoEntity, T>() {
            @Override
            public T transform(DaoEntity _daoEntity) {
                return DaoSerializer.fromDaoEntity(_daoEntity, _class, getType());
            }
        });
    }
    protected DaoQuery prepareQuery(DaoQuery _query) {
        if (queryPreparer == null)
            return _query;
        return queryPreparer.prepareQuery(_query);
    }
}
