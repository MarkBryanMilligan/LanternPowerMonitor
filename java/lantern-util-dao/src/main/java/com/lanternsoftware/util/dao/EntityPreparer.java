package com.lanternsoftware.util.dao;

import java.util.Collection;

import com.lanternsoftware.util.CollectionUtils;

public abstract class EntityPreparer {
    public Collection<DaoEntity> prepareEntities(Collection<DaoEntity> _entities) {
        for (DaoEntity entity : CollectionUtils.makeNotNull(_entities)) {
            prepareEntity(entity);
        }
        return _entities;
    }

    public abstract void prepareEntity(DaoEntity _entity);
}
