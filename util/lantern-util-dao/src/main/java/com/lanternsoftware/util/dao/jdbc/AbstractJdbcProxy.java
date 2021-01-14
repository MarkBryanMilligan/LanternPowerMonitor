package com.lanternsoftware.util.dao.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.ITransformer;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.AbstractDaoProxy;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.DaoSort;
import com.lanternsoftware.util.dao.DaoSortField;
import com.lanternsoftware.util.dao.EntityPreparer;
import com.lanternsoftware.util.dao.annotations.DBClob;
import com.lanternsoftware.util.dao.annotations.DBSerializable;
import com.lanternsoftware.util.dao.annotations.NeverUpdate;
import com.lanternsoftware.util.dao.annotations.PrimaryKey;
import com.lanternsoftware.util.dao.jdbc.preparedinstatement.PreparedInStatement;

public abstract class AbstractJdbcProxy extends AbstractDaoProxy {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJdbcProxy.class);
    protected final Map<String, String> insertStatements = new HashMap<String, String>();
    protected final Map<String, String> updateStatements = new HashMap<String, String>();
    protected DatabaseType databaseType = DatabaseType.ORACLE_11G;
    protected EntityPreparer entityPreparer;
    private int fetchSize = 500;

    @Override
    public DaoProxyType getType() {
        return DaoProxyType.JDBC;
    }

    public abstract Connection getConnection();
    
    public abstract boolean isConnected();
    
    public abstract boolean alwaysClose();

    public void setFetchSize(int _fetchSize) {
        fetchSize = _fetchSize;
    }

    public EntityPreparer getEntityPreparer() {
        return entityPreparer;
    }

    public void setEntityPreparer(EntityPreparer _entityPreparer) {
        entityPreparer = _entityPreparer;
    }

    public <T> List<T> query(final Class<T> _class, String _sql, DaoQuery _query) {
        return toObjects(queryForEntities(_query, _sql), _class);
    }
    
    @Override
    public int count(String _tableName, DaoQuery _query) {
        ResultSet rs = null;
        PreparedStatement statement = null;
        try {
            int count = 0;
            String sql = buildSQL("SELECT COUNT(*)", _tableName, _query, null, 0, -1);
            if (sql.contains("{in}")) {
                PreparedInStatement inStatement = getPreparedInStatement(sql);
                populateStatement(inStatement, 1, _query);
                Connection conn = null;
                while (inStatement.hasNext()) {
                    rs = inStatement.next();
                    if (rs.next()) {
                        count += rs.getInt(1);
                    }
                    Statement st = rs.getStatement();
                    conn = st.getConnection();
                    close(rs);
                    close(st);
                }
                close(conn);
            }
            else {
                statement = getPreparedStatement(sql);
                populateStatement(statement, 1, _query);
                rs = statement.executeQuery();
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }
            return count;
        }
        catch (SQLException e) {
            LOG.error("Failed to read count", e);
            return 0;
        }
        finally {
            close(rs);
            closeStatementAndConnection(statement);
        }
    }

    @Override
    public List<DaoEntity> queryForEntities(String _tableName, DaoQuery _query, Collection<String> _fields, DaoSort _sort, int _offset, int _count) {
        StringBuilder operation = null;
        if (CollectionUtils.isEmpty(_fields))
            operation = new StringBuilder("SELECT *");
        else {
            for (String sField : _fields) {
                if (operation == null) {
                    operation = new StringBuilder("SELECT ");
                }
                else {
                    operation.append(", ");
                }
                operation.append(sField);
            }
        }
        return queryForEntities(_query, buildSQL(operation.toString(), _tableName, _query, _sort, _offset, _count));
    }
    
    protected List<DaoEntity> queryForEntities(DaoQuery _query, String _sql) {
        LOG.debug(_sql);
        if (_sql.contains("{in}")) {
            PreparedInStatement statement = getPreparedInStatement(_sql);
            populateStatement(statement, 1, _query);
            return toDaoEntities(statement);
        }
        PreparedStatement statement = getPreparedStatement(_sql);
        populateStatement(statement, 1, _query);
        return toDaoEntities(statement);
    }
    
    protected String buildSQL(String _sOperation, String _tableName, DaoQuery _object, DaoSort _sort, int _offset, int _count) {
        StringBuilder builder = new StringBuilder(_sOperation);
        builder.append(" FROM ");
        builder.append(_tableName);
        if ((_object != null) && !_object.isEmpty()) {
            builder.append(" WHERE ");
            queryToSQL(_object, builder);
        }
        if (_sort != null) {
            StringBuilder sort = null;
            for (DaoSortField sortField : _sort.getFields()) {
                if (sort == null) {
                    sort = new StringBuilder(" ORDER BY ");
                }
                else {
                    sort.append(", ");
                }
                if (sortField.isIgnoreCase())
                    sort.append("UPPER(");
                sort.append(sortField.getField());
                if (sortField.isIgnoreCase())
                    sort.append(")");
                if (!sortField.isAscending()) {
                    sort.append(" DESC");
                }
            }
            if (sort != null) {
                builder.append(sort);
            }
        }
        if ((_offset == 0) && (_count <= 0))
            return builder.toString();
        if (databaseType == DatabaseType.ORACLE_11G) {
            StringBuilder paged = new StringBuilder();
            if (_offset > 0)
                paged.append("select * from (");
            paged.append("SELECT t.*, ROWNUM rn FROM (");
            paged.append(builder.toString());
            paged.append(") t");
            if (_count > 0) {
                paged.append(" WHERE ROWNUM <= ");
                paged.append(_offset + _count);
            }
            if (_offset > 0) {
                paged.append(") WHERE rn > ");
                paged.append(_offset);
            }
            return paged.toString();
        }
        else if (databaseType == DatabaseType.MYSQL) {
            builder.append(" limit ");
            builder.append(_offset);
            if (_count > 0) {
                builder.append(",");
                builder.append(_count);
            }
        }
        return builder.toString();
    }
    
    protected void queryToSQL(DaoQuery _object, StringBuilder _builder) {
        if (_object == null)
            return;
        Iterator<Map.Entry<String, Object>> iterEntries = _object.entrySet().iterator();
        Map.Entry<String, Object> entry = iterEntries.next();
        objectToSQL(entry.getKey(), entry.getValue(), _builder);
        while (iterEntries.hasNext()) {
            entry = iterEntries.next();
            if(!entry.getKey().equals("$or")) {
                _builder.append(" and ");
            }
            else {
                _builder.append(" or (");
            }
            objectToSQL(entry.getKey(), entry.getValue(), _builder);
        }
    }
    
    protected void objectToSQL(String _sName, Object _value, StringBuilder _builder) {
        if (_sName.equals("$or") && (_value instanceof DaoQuery)) {
            DaoQuery query = (DaoQuery) _value;
            queryToSQL(query, _builder);
            _builder.append(")");
            return;
        }
        if (_sName.equals("$and") && (_value instanceof DaoQuery[])) {
            DaoQuery[] values = (DaoQuery[]) _value;
            if (values.length > 1) {
                queryToSQL(values[0], _builder);
                for (int i = 1; i < values.length; i++) {
                    _builder.append(" and ");
                    queryToSQL(values[i], _builder);
                }
            }
        }
        else if (_value instanceof DaoQuery) {
            DaoQuery child = (DaoQuery) _value;
            boolean first = true;
            if (child.containsKey("$equalIgnoreCase")) {
                if (!first)
                    _builder.append(" and ");
                _builder.append("lower(");
                _builder.append(_sName);
                _builder.append(")");
                _builder.append(" = ?");
                first = false;
            }
            if (child.containsKey("$ne")) {
                if (!first)
                    _builder.append(" and ");
                _builder.append(_sName);
                _builder.append(" != ?");
                first = false;
            }
            if (child.containsKey("$gt")) {
                if (!first)
                    _builder.append(" and ");
                _builder.append(_sName);
                _builder.append(" > ?");
                first = false;
            }
            if (child.containsKey("$lt")) {
                if (!first)
                    _builder.append(" and ");
                _builder.append(_sName);
                _builder.append(" < ?");
                first = false;
            }
            if (child.containsKey("$gte")) {
                if (!first)
                    _builder.append(" and ");
                _builder.append(_sName);
                _builder.append(" >= ?");
                first = false;
            }
            if (child.containsKey("$lte")) {
                if (!first)
                    _builder.append(" and ");
                _builder.append(_sName);
                _builder.append(" <= ?");
                first = false;
            }
            if (child.containsKey("$contains") || child.containsKey("$startsWith")) {
                if (!first)
                    _builder.append(" and ");
                _builder.append(_sName);
                _builder.append(" like ?");
                first = false;
            }
            if (child.containsKey("$containsIgnoreCase") || child.containsKey("$startsWithIgnoreCase")) {
                if (!first)
                    _builder.append(" and ");
                _builder.append("lower(");
                _builder.append(_sName);
                _builder.append(")");
                _builder.append(" like ?");
                first = false;
            }
            if (child.containsKey("$in")) {
                if (!first)
                    _builder.append(" and ");
                _builder.append("{in}");
                first = false;
            }
            if (child.containsKey("$nin")) {
                if (!first)
                    _builder.append(" and ");
                _builder.append("NOT {in}");
                first = false;
            }
            if (child.containsKey("$or")) {
                _builder.append("?");
            }
            if (child.containsKey("$orEqualsIgnoreCase")) {
                _builder.append("lower(");
                _builder.append(_sName);
                _builder.append(")");
                _builder.append(" = ?");
            }

        }
        else if ((_value instanceof String) && NullUtils.isEqual(_value, "$null")) {
            _builder.append(_sName);
            _builder.append(" IS NULL");
        }
        else if ((_value instanceof String) && NullUtils.isEqual(_value, "$notnull")) {
            _builder.append(_sName);
            _builder.append(" IS NOT NULL");
        }
        else {
            _builder.append(_sName);
            _builder.append(" = ?");
        }
    }
    
    protected int populateStatement(PreparedStatement _statement, int _iParam, DaoQuery _query) {
        try {
            for (Map.Entry<String, Object> entry : CollectionUtils.makeNotNull(_query).entrySet()) {
                _iParam = setSQLValue(_statement, _iParam, entry.getKey(), entry.getValue());
            }
        }
        catch (SQLException e) {
            LOG.error("Failed to populate statement", e);
        }
        return _iParam;
    }
    
    protected int setSQLValue(PreparedStatement _statement, int _iParam, String _sName, Object _value) throws SQLException {
        if (_sName.equals("$and") && (_value instanceof DaoQuery[])) {
            for (DaoQuery query : (DaoQuery[]) _value) {
                _iParam = populateStatement(_statement, _iParam, query);
            }
        }
        if (_sName.equals("$or") && (_value instanceof DaoQuery)) {
            DaoQuery query = (DaoQuery) _value;
            _iParam = populateStatement(_statement, _iParam, query);
        }
        else if (_value instanceof DaoQuery) {
            DaoQuery child = (DaoQuery) _value;
            _iParam = setParam(_statement, _iParam, child.get("$ne"));
            _iParam = setParam(_statement, _iParam, child.get("$gt"));
            _iParam = setParam(_statement, _iParam, child.get("$lt"));
            _iParam = setParam(_statement, _iParam, child.get("$gte"));
            _iParam = setParam(_statement, _iParam, child.get("$lte"));
            Object contains = child.get("$contains");
            if (contains instanceof String) {
                StringBuilder param = new StringBuilder("%");
                param.append((String)contains);
                param.append("%");
                _iParam = setParam(_statement, _iParam, param.toString());
            }
            Object containsIgnoreCase = child.get("$containsIgnoreCase");
            if (containsIgnoreCase instanceof String) {
                StringBuilder param = new StringBuilder("%");
                param.append(((String)containsIgnoreCase).toLowerCase());
                param.append("%");
                _iParam = setParam(_statement, _iParam, param.toString());
            }
            Object equalIgnoreCase = child.get("$equalIgnoreCase");
            if (equalIgnoreCase instanceof String) {
                StringBuilder param = new StringBuilder(((String)equalIgnoreCase).toLowerCase());
                _iParam = setParam(_statement, _iParam, param.toString());
            }
            Object startsWith = child.get("$startsWith");
            if (startsWith instanceof String) {
                StringBuilder param = new StringBuilder((String)startsWith);
                param.append("%");
                _iParam = setParam(_statement, _iParam, param.toString());
            }
            Object startsWithIgnoreCase = child.get("$startsWithIgnoreCase");
            if (startsWithIgnoreCase instanceof String) {
                StringBuilder param = new StringBuilder(((String)startsWithIgnoreCase).toLowerCase());
                param.append("%");
                _iParam = setParam(_statement, _iParam, param.toString());
            }
            Object or = child.get("$or");
            if (or instanceof String) {
                StringBuilder param = new StringBuilder(((String)or));
                _iParam = setParam(_statement, _iParam, param.toString());
            }
            Object orEqualIgnoreCase = child.get("$orEqualIgnoreCase");
            if (orEqualIgnoreCase instanceof String) {
                StringBuilder param = new StringBuilder(((String)orEqualIgnoreCase).toLowerCase());
                _iParam = setParam(_statement, _iParam, param.toString());
            }
        }
        else {
            _iParam = setParam(_statement, _iParam, _value);
        }
        return _iParam;
    }
    
    protected int setParam(PreparedStatement _statement, int _iParam, Object _value) throws SQLException {
        if ((_value == null) || ((_value instanceof String) && NullUtils.isOneOf(_value, "$null", "$notnull"))) {
            return _iParam;
        }
        _statement.setObject(_iParam, _value);
        return ++_iParam;
    }
    
    protected int populateStatement(PreparedInStatement _statement, int _iParam, DaoQuery _object) {
        try {
            for (Map.Entry<String, Object> entry : CollectionUtils.makeNotNull(_object).entrySet()) {
                _iParam = setSQLValue(_statement, _iParam, entry.getKey(), entry.getValue());
            }
        }
        catch (SQLException e) {
            LOG.error("Failed to populate in statement", e);
        }
        return _iParam;
    }
    
    protected int setSQLValue(PreparedInStatement _statement, int _iParam, String _sName, Object _value) throws SQLException {
        if (_sName.equals("$and") && (_value instanceof DaoQuery[])) {
            for (DaoQuery object : (DaoQuery[]) _value) {
                _iParam = populateStatement(_statement, _iParam, object);
            }
        }
        else if (_value instanceof DaoQuery) {
            DaoQuery child = (DaoQuery) _value;
            Object inClause = child.get("$in");
            if (inClause == null)
                inClause = child.get("$nin");
            if (inClause instanceof Object[]) {
                Collection<Object> collObjects = new ArrayList<Object>();
                Collections.addAll(collObjects, (Object[]) inClause);
                _statement.setInClause(_iParam++, _sName, collObjects);
            }
            else if (inClause instanceof Collection<?>) {
                _statement.setInClause(_iParam++, _sName, (Collection<Object>) inClause);
            }
            _iParam = setParam(_statement, _iParam, child.get("$ne"));
            _iParam = setParam(_statement, _iParam, child.get("$gt"));
            _iParam = setParam(_statement, _iParam, child.get("$lt"));
            _iParam = setParam(_statement, _iParam, child.get("$gte"));
            _iParam = setParam(_statement, _iParam, child.get("$lte"));
            Object contains = child.get("$contains");
            if (contains instanceof String) {
                StringBuilder param = new StringBuilder("%");
                param.append((String)contains);
                param.append("%");
                _iParam = setParam(_statement, _iParam, param.toString());
            }
            Object containsIgnoreCase = child.get("$containsIgnoreCase");
            if (containsIgnoreCase instanceof String) {
                StringBuilder param = new StringBuilder("%");
                param.append(((String)containsIgnoreCase).toLowerCase());
                param.append("%");
                _iParam = setParam(_statement, _iParam, param.toString());
            }
            Object equalIgnoreCase = child.get("$equalIgnoreCase");
            if (equalIgnoreCase instanceof String) {
                StringBuilder param = new StringBuilder(((String)equalIgnoreCase).toLowerCase());
                _iParam = setParam(_statement, _iParam, param.toString());
            }
            Object startsWith = child.get("$startsWith");
            if (startsWith instanceof String) {
                StringBuilder param = new StringBuilder((String)startsWith);
                param.append("%");
                _iParam = setParam(_statement, _iParam, param.toString());
            }
            Object startsWithIgnoreCase = child.get("$startsWithIgnoreCase");
            if (startsWithIgnoreCase instanceof String) {
                StringBuilder param = new StringBuilder(((String)startsWithIgnoreCase).toLowerCase());
                param.append("%");
                _iParam = setParam(_statement, _iParam, param.toString());
            }
            Object or = child.get("$or");
            if (or instanceof String) {
                StringBuilder param = new StringBuilder(((String)or));
                _iParam = setParam(_statement, _iParam, param.toString());
            }
            Object orEqualIgnoreCase = child.get("$orEqualIgnoreCase");
            if (orEqualIgnoreCase instanceof String) {
                StringBuilder param = new StringBuilder(((String)orEqualIgnoreCase).toLowerCase());
                _iParam = setParam(_statement, _iParam, param.toString());
            }
        }
        else {
            _iParam = setParam(_statement, _iParam, _value);
        }
        return _iParam;
    }
    
    protected int setParam(PreparedInStatement _statement, int _iParam, Object _value) throws SQLException {
        if ((_value == null) || ((_value instanceof String) && NullUtils.isOneOf(_value, "$null", "$notnull"))) {
            return _iParam;
        }
        _statement.setObject(_iParam, _value);
        return ++_iParam;
    }

    protected List<DaoEntity> toDaoEntities(PreparedInStatement _statement) {
        List<DaoEntity> listObjects = new ArrayList<DaoEntity>();
        ResultSet rs = null;
        Connection conn = null;
        try {
            while (_statement.hasNext()) {
                rs = _statement.next();
                toDaoEntities(rs, listObjects);
                Statement statement = rs.getStatement();
                conn = statement.getConnection();
                close(rs);
                close(statement);
            }
        }
        catch (SQLException e) {
            LOG.error("Failed to read result set", e);
        }
        finally {
            close(rs);
            close(conn);
        }
        return listObjects;
    }
    
    protected List<DaoEntity> toDaoEntities(PreparedStatement _statement) {
        List<DaoEntity> listObjects = new ArrayList<DaoEntity>();
        ResultSet rs = null;
        try {
            rs = _statement.executeQuery();
            toDaoEntities(rs, listObjects);
        }
        catch (SQLException e) {
            LOG.error("Failed to read result set", e);
        }
        finally {
            close(rs);
            closeStatementAndConnection(_statement);
        }
        return listObjects;
    }
    
    protected void toDaoEntities(ResultSet _rs, Collection<DaoEntity> _collEntities) {
        try {
            while (_rs.next()) {
                DaoEntity object = new DaoEntity();
                ResultSetMetaData rsmd = _rs.getMetaData();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    int type = rsmd.getColumnType(i);
                    String name = rsmd.getColumnName(i).toLowerCase();
                    if (type == Types.TIMESTAMP) // This prevents Oracle from using a non-JDBC compliant type.
                    {
                        object.put(name, _rs.getTimestamp(i));
                    }
                    else if (type == Types.BLOB) {
                        InputStream is = null;
                        java.sql.Blob blob = null;
                        try {
                            blob = _rs.getBlob(i);
                            is = blob.getBinaryStream();
                            object.put(name, IOUtils.toByteArray(is));
                        }
                        catch (Exception e) {
                            close(blob);
                            IOUtils.closeQuietly(is);
                        }
                    }
                    else if (type == Types.CLOB) {
                        Reader r = null;
                        Clob clob = null;
                        try {
                            clob = _rs.getClob(i);
                            r = clob.getCharacterStream();
                            object.put(name, IOUtils.toString(r));
                        }
                        catch (Exception e) {
                            close(clob);
                            IOUtils.closeQuietly(r);
                        }
                    }
                    else {
                        object.put(name, _rs.getObject(i));
                    }
                }
                _collEntities.add(object);
            }
        }
        catch (SQLException e) {
            LOG.error("Failed to read result set", e);
        }
    }
    
    @Override
    public boolean delete(String _tableName, DaoQuery _query) {
        PreparedStatement statement = null;
        try {
            String query = buildSQL("DELETE", _tableName, _query, null, 0, -1);
            if (query.contains("{in}")) {
                PreparedInStatement inStatement = getPreparedInStatement(query);
                populateStatement(inStatement, 1, _query);
                inStatement.executeUpdate();
            }
            else {
                statement = getPreparedStatement(query);
                populateStatement(statement, 1, _query);
                statement.executeUpdate();
                Connection conn = statement.getConnection();
                close(statement);
                close(conn);
            }
            return true;
        }
        catch (SQLException e) {
            LOG.error("Failed to delete record", e);
            return false;
        }
        finally {
            closeStatementAndConnection(statement);
        }
    }
    
    @Override
    public void update(Class<?> _class, DaoQuery _query, DaoEntity _changes) {
        if (_changes == null) {
            return;
        }
        List<Clob> clobs = new ArrayList<Clob>();
        PreparedStatement statement = null;
        try {
            String sQuery = buildUpdateStatement(DaoSerializer.getTableName(_class, getType()), _query, _changes);
            if (sQuery.contains("{in}")) {
                PreparedInStatement inStatement = getPreparedInStatement(sQuery);
                int iIdx = 0;
                for (Map.Entry<String, Object> field : _changes.entrySet()) {
                    if (field.getValue() == null)
                        inStatement.setNull(++iIdx, DaoSerializer.getSqlType(_class, field.getKey()));
                    else
                        inStatement.setObject(++iIdx, field.getValue());
                }
                populateStatement(inStatement, ++iIdx, _query);
                inStatement.executeUpdate();
            }
            else {
                Set<String> clobFields = new HashSet<String>(DaoSerializer.getFieldsByAnnotation(_class, DBClob.class, getType()));
                Connection connection = getConnection();
                statement = getPreparedStatement(connection, sQuery);
                int iIdx = 0;
                for (Map.Entry<String, Object> field : _changes.entrySet()) {
                    setStatementField(connection, statement, ++iIdx, field.getKey(), field.getValue(), _class, clobFields, clobs);
                }
                populateStatement(statement, ++iIdx, _query);
                statement.executeUpdate();
            }
        }
        catch (SQLException e) {
            LOG.error("Failed to execute update", e);
        }
        finally {
            for (Clob clob : clobs) {
                try {
                    clob.free();
                }
                catch (SQLException _e) {
                    LOG.error("Failed to free clob", _e);
                }
            }
            closeStatementAndConnection(statement);
        }
    }

    @Override
    public <T> T updateOne(Class<T> _class, DaoQuery _query, DaoEntity _changes) {
        update(_class, _query, _changes);
        return queryOne(_class, _query);
    }

    @Override
    public <T> Map<String, T> save(Collection<T> _objects) {
        return save(_objects, false, true);
    }

    public <T> Map<String, T> save(Collection<T> _objects, boolean _checkExists, boolean _savepoint) {
        Map<String, T> ids = new HashMap<String, T>();
        T object = CollectionUtils.getFirst(_objects);
        if (object == null)
            return ids;
        Class<?> entityClass = object.getClass();
        String tableName = DaoSerializer.getTableName(entityClass, getType());
        List<String> primaryKeyFields = DaoSerializer.getFieldsByAnnotation(entityClass, PrimaryKey.class, getType());
        if (primaryKeyFields.size() > 1)
            throw new RuntimeException("Objects with multiple primary keys are not supported for batch inserts");
        if (primaryKeyFields.size() == 0)
            throw new RuntimeException("No primary key annotated on entity class " + entityClass.getCanonicalName());
        final String primaryKeyField = primaryKeyFields.get(0);
        List<DaoEntity> insertEntities = new ArrayList<DaoEntity>();
        List<DaoEntity> updateEntities = new ArrayList<DaoEntity>();
        Object primaryKey = null;
        for (T o : _objects) {
            DaoEntity entity = DaoSerializer.toDaoEntity(o, getType());
            prepareEntity(entity);
            primaryKey = entity.get(primaryKeyField);
            if ((primaryKey == null) || (primaryKey instanceof String)) {
                if (NullUtils.isEmpty((String) primaryKey)) {
                    primaryKey = UUID.randomUUID().toString();
                    entity.put(primaryKeyField, primaryKey);
                    insertEntities.add(entity);
                }
                else {
                    updateEntities.add(entity);
                }
                ids.put((String)primaryKey, o);
            }
            else {
                throw new RuntimeException("Only String primary keys are supported for batch inserts");
            }
        }
        if (_checkExists && !updateEntities.isEmpty()) {
            Set<String> entities = CollectionUtils.transformToSet(updateEntities, new ITransformer<DaoEntity, String>() {
                @Override
                public String transform(DaoEntity _daoEntity) {
                    return (String)_daoEntity.get(primaryKeyField);
                }
            });
            for (String id : queryForField(entityClass, DaoQuery.in(primaryKeyField, entities), primaryKeyField)) {
                entities.remove(id);
            }
            Iterator<DaoEntity> iterUpdates = updateEntities.iterator();
            while (iterUpdates.hasNext()) {
                DaoEntity entity = iterUpdates.next();
                if (entities.contains((String)entity.get(primaryKeyField))) {
                    iterUpdates.remove();
                    insertEntities.add(entity);
                }
            }
        }
        Set<String> clobFields = new HashSet<String>(DaoSerializer.getFieldsByAnnotation(entityClass, DBClob.class, getType()));
        List<Clob> clobs = new ArrayList<Clob>();
        Connection connection = null;
        Savepoint sp = null;
        PreparedStatement insert = null;
        PreparedStatement update = null;
        try {
            connection = getConnection();
            if (connection != null) {
                boolean autoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                if (_savepoint)
                    sp = connection.setSavepoint();
                DaoEntity insertEntity = CollectionUtils.getFirst(insertEntities);
                if (insertEntity != null) {
                    primaryKey = insertEntity.remove(primaryKeyField);
                    insert = getPreparedStatement(connection, getInsertStatement(tableName, insertEntity, primaryKeyFields));
                    insertEntity.put(primaryKeyField, primaryKey);
                    if (insert == null)
                        return ids;
                    for (DaoEntity entity : insertEntities) {
                        int iIdx = 0;
                        primaryKey = entity.remove(primaryKeyField);
                        insert.setObject(++iIdx, primaryKey);
                        for (Map.Entry<String, Object> field : entity.entrySet()) {
                            setStatementField(connection, insert, ++iIdx, field.getKey(), field.getValue(), entityClass, clobFields, clobs);
                        }
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
                DaoEntity updateEntity = CollectionUtils.getFirst(updateEntities);
                if (updateEntity != null) {
                    DaoQuery updateQuery = new DaoQuery();
                    for (String key : primaryKeyFields) {
                        updateQuery.put(key, "");
                    }
                    primaryKey = updateEntity.remove(primaryKeyField);
                    Map<String, Object> ignored = new HashMap<String, Object>();
                    for (String ignore : DaoSerializer.getFieldsByAnnotation(entityClass, NeverUpdate.class, getType())) {
                        ignored.put(ignore, updateEntity.remove(ignore));
                    }
                    update = getPreparedStatement(connection, getUpdateStatement(tableName, updateQuery, updateEntity));
                    updateEntity.put(primaryKeyField, primaryKey);
                    for (Entry<String, Object> ignoredEntry : ignored.entrySet()) {
                        updateEntity.put(ignoredEntry.getKey(), ignoredEntry.getValue());
                    }
                    if (update == null) {
                        if (sp != null)
                            connection.rollback(sp);
                        return ids;
                    }
                    for (DaoEntity entity : updateEntities) {
                        for (String ignore : DaoSerializer.getFieldsByAnnotation(entityClass, NeverUpdate.class, getType())) {
                            entity.remove(ignore);
                        }
                        primaryKey = entity.remove(primaryKeyField);
                        int iIdx = 0;
                        for (Map.Entry<String, Object> field : entity.entrySet()) {
                            setStatementField(connection, update, ++iIdx, field.getKey(), field.getValue(), entityClass, clobFields, clobs);
                        }
                        update.setObject(++iIdx, primaryKey);
                        update.addBatch();
                    }
                    update.executeBatch();
                }
                connection.commit();
                connection.setAutoCommit(autoCommit);
            }
            return ids;
        }
        catch (SQLException _e) {
            LOG.error("Exception occurred while batch inserting", _e);
            try {
                if (sp != null)
                    connection.rollback(sp);
            }
            catch (SQLException _e1) {
                LOG.error("Failed to rollback after exception", _e1);
            }
            return ids;
        }
        finally {
            for (Clob clob : clobs) {
                close(clob);
            }
            close(insert);
            close(update);
            close(connection);
        }
    }

    private void setStatementField(Connection _connection, PreparedStatement _statement, int _idx, String _name, Object _value, Class<?> _entityClass, Set<String> _clobFields, List<Clob> _clobs) throws SQLException {
        if (_clobFields.contains(_name)) {
            Clob clob = _connection.createClob();
            clob.setString(1, (String) _value);
            _clobs.add(clob);
            _statement.setClob(_idx, clob);
        }
        else {
            if (_value == null)
                _statement.setNull(_idx, DaoSerializer.getSqlType(_entityClass, _name));
            else
                _statement.setObject(_idx, _value);
        }
    }

    @Override
    public String saveEntity(String _collection, DaoEntity _entity) {
        return saveEntity(_collection, _entity, "id");
    }

    public String saveEntity(String _collection, DaoEntity _entity, String _primaryKeyField) {
        return saveEntity(null, _collection, null, CollectionUtils.asArrayList(_primaryKeyField), null, null, _entity);
    }

    @Override
    public String saveEntity(Class<?> _class, DaoEntity _entity) {
        DBSerializable table = _class.getAnnotation(DBSerializable.class);
        String seq = (table == null)?null:table.seq();
        return saveEntity(_class, DaoSerializer.getTableName(_class, getType()), seq, DaoSerializer.getFieldsByAnnotation(_class, PrimaryKey.class, getType()), DaoSerializer.getFieldsByAnnotation(_class, DBClob.class, getType()), DaoSerializer.getFieldsByAnnotation(_class, NeverUpdate.class, getType()), _entity);
    }

    private String saveEntity(Class<?> _class, String _table, String _sequenceName, List<String> _primaryKeys, List<String> _clobs, List<String> _neverUpdate, DaoEntity _entity) {
        if (_entity == null) {
            return null;
        }
        prepareEntity(_entity);
        Set<String> clobFields = new HashSet<>(CollectionUtils.makeNotNull(_clobs));
        DaoQuery keyQuery = new DaoQuery();
        boolean insert = false;
        List<Object> keys = new ArrayList<Object>();
        for (String key : CollectionUtils.makeNotNull(_primaryKeys)) {
            Object value = _entity.remove(key);
            if ((value == null) || (value instanceof String)) {
                if (NullUtils.isEmpty((String) value)) {
                    value = UUID.randomUUID().toString();
                    insert = true;
                }
            }
            else if (value instanceof Long) {
                if (((Long) value) == 0) {
                    value = getNextSequence(_sequenceName, _table);
                    if (((Long) value) == 0){
                        value = null; //null value will use mySQL auto-increment by default
                    }
                    insert = true;
                }
            }
            keyQuery.put(key, value);
            keys.add(value);
        }
        if (!insert)
            insert = !exists(_table, keyQuery);
        String sQuery;
        if (insert) {
            sQuery = getInsertStatement(_table, _entity, _primaryKeys);
        }
        else {
            DaoQuery updateQuery = new DaoQuery();
            for (String key : _primaryKeys) {
                updateQuery.put(key, "");
            }
            for (String ignore : CollectionUtils.makeNotNull(_neverUpdate)) {
                _entity.remove(ignore);
            }
            sQuery = getUpdateStatement(_table, updateQuery, _entity);
        }
        List<Clob> clobs = new ArrayList<Clob>();
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try {
            statement = getPreparedStatement(connection, sQuery);
            int iIdx = 0;
            if (insert) {
                for (Object id : keys) {
                    statement.setObject(++iIdx, id);
                }
            }
            for (Map.Entry<String, Object> field : _entity.entrySet()) {
                setStatementField(connection, statement, ++iIdx, field.getKey(), field.getValue(), _class, clobFields, clobs);
            }
            if (!insert) {
                for (Object id : keys) {
                    statement.setObject(++iIdx, id);
                }
            }
            final int id = statement.executeUpdate();
            return CollectionUtils.transformToCommaSeparated(keys, new ITransformer<Object, String>() {
                @Override
                public String transform(Object _o) {
                    if (_o instanceof String)
                        return (String) _o;
                    if (_o instanceof Long)
                        return String.valueOf((Long) _o);
                    if(_o instanceof Integer)
                        return String.valueOf((Integer) id);
                    return null;
                }
            });
        }
        catch (SQLException e) {
            LOG.error("Failed to save entity", e);
            return "";
        }
        finally {
            for (Clob clob : clobs) {
                close(clob);
            }
            close(statement);
            close(connection);
        }
    }

    protected String getInsertStatement(String _tableName, DaoEntity _entity, List<String> _primaryKeyFields) {
        String sQuery = insertStatements.get(_tableName);
        if (sQuery != null) {
            return sQuery;
        }
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append(_tableName);
        builder.append("(");
        builder.append(CollectionUtils.commaSeparated(_primaryKeyFields));
        if(CollectionUtils.isNotEmpty(_entity)) {
            builder.append(",");
            builder.append(CollectionUtils.commaSeparated(_entity.keySet()));
        }
        
        StringBuilder values = null;
        for (int i = 0; i < _primaryKeyFields.size() + CollectionUtils.size(_entity); i++) {
            if (values == null) {
                values = new StringBuilder();
            }
            else {
                values.append(",");
            }
            values.append("?");
        }
        builder.append(") values(");
        builder.append(values);
        builder.append(")");
        sQuery = builder.toString();
        insertStatements.put(_tableName, sQuery);
        return sQuery;
    }
    
    protected String getUpdateStatement(String _tableName, DaoQuery _query, DaoEntity _object) {
        String sQuery = updateStatements.get(_tableName);
        if (sQuery != null) {
            return sQuery;
        }
        sQuery = buildUpdateStatement(_tableName, _query, _object);
        updateStatements.put(_tableName, sQuery);
        return sQuery;
    }
    
    protected String buildUpdateStatement(String _tableName, DaoQuery _query, DaoEntity _object) {
        StringBuilder builder = new StringBuilder("UPDATE ");
        builder.append(_tableName);
        builder.append(" SET ");
        Iterator<String> iterColumns = _object.keySet().iterator();
        if (!iterColumns.hasNext()) {
            return "";
        }
        builder.append(iterColumns.next());
        builder.append(" = ?");
        while (iterColumns.hasNext()) {
            builder.append(",");
            builder.append(iterColumns.next());
            builder.append(" = ?");
        }
        builder.append(" where ");
        queryToSQL(_query, builder);
        return builder.toString();
    }
    
    protected void close(ResultSet _rs) {
        try {
            if (_rs != null) {
                _rs.close();
            }
        }
        catch (SQLException _e) {
        }
    }
    
    protected void closeStatementAndConnection(Statement _statement) {
        if (_statement == null)
            return;
        try {
            Connection conn = _statement.getConnection();
            close(_statement);
            if (alwaysClose())
                conn.close();
        }
        catch (SQLException _e) {
        }
    }

    protected void close(Connection _connection) {
        try {
            if ((_connection != null) && alwaysClose()) {
                _connection.close();
            }
        }
        catch (SQLException _e) {
        }
    }

    protected void close(Statement _statement) {
        try {
            if (_statement != null) {
                _statement.close();
            }
        }
        catch (SQLException _e) {
        }
    }
    
    protected void close(Clob _clob) {
        try {
            if (_clob != null) {
                _clob.free();
            }
        }
        catch (SQLException _e) {
        }
    }
    
    protected void close(Blob _blob) {
        try {
            if (_blob != null) {
                _blob.free();
            }
        }
        catch (SQLException _e) {
        }
    }
    
    public PreparedStatement getPreparedStatement(String _statement) {
        if (_statement == null) {
            return null;
        }
        LOG.trace(_statement);
        return getPreparedStatement(getConnection(), _statement);
    }

    public PreparedStatement getPreparedStatement(Connection _connection, String _statement) {
        try {
            PreparedStatement statement = _connection.prepareStatement(_statement);
            statement.setFetchSize(fetchSize);
            return statement;
        }
        catch (SQLException e) {
            LOG.error("Failed to create PreparedStatement", e);
            return null;
        }
    }
    
    protected PreparedInStatement getPreparedInStatement(String _statement) {
        if (_statement == null) {
            return null;
        }
        LOG.trace(_statement);
        try {
            PreparedInStatement statement = new PreparedInStatement(_statement, this);
            return statement;
        }
        catch (SQLException e) {
            LOG.error("Failed to create PreparedInStatement", e);
            return null;
        }
    }
    
    private long getNextSequence(String _sequence, String _tableName) {
        if (NullUtils.isEmpty(_sequence)) {
            LOG.error("Failed to get sequence name for " + _tableName);
            return 0;
        }
        PreparedStatement statement = getPreparedStatement("SELECT " + _sequence + ".NEXTVAL FROM DUAL");
        ResultSet rs = null;
        try {
            rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            LOG.error("Failed to get sequence name for " + _tableName);
            return 0;
        }
        catch (SQLException e) {
            LOG.error("Failed to get sequence name for " + _tableName);
            return 0;
        }
        finally {
            close(rs);
            closeStatementAndConnection(statement);
        }
    }

    private void prepareEntity(DaoEntity _entity) {
        if (entityPreparer != null)
            entityPreparer.prepareEntity(_entity);
    }
}
