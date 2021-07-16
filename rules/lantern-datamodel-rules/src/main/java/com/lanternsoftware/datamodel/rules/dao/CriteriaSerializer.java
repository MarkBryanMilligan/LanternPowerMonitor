package com.lanternsoftware.datamodel.rules.dao;

import com.lanternsoftware.datamodel.rules.Criteria;
import com.lanternsoftware.datamodel.rules.EventType;
import com.lanternsoftware.datamodel.rules.Operator;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class CriteriaSerializer extends AbstractDaoSerializer<Criteria>
{
	@Override
	public Class<Criteria> getSupportedClass()
	{
		return Criteria.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(Criteria _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("type", DaoSerializer.toEnumName(_o.getType()));
		d.put("source_id", _o.getSourceId());
		d.put("operator", DaoSerializer.toEnumName(_o.getOperator()));
		d.put("value", _o.getValue());
		d.put("or", _o.isOr());
		d.put("criteria", DaoSerializer.toDaoEntities(_o.getCriteria(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public Criteria fromDaoEntity(DaoEntity _d)
	{
		Criteria o = new Criteria();
		o.setType(DaoSerializer.getEnum(_d, "type", EventType.class));
		o.setSourceId(DaoSerializer.getString(_d, "source_id"));
		o.setOperator(DaoSerializer.getEnum(_d, "operator", Operator.class));
		o.setValue(DaoSerializer.getDouble(_d, "value"));
		o.setOr(DaoSerializer.getBoolean(_d, "or"));
		o.setCriteria(DaoSerializer.getList(_d, "criteria", Criteria.class));
		return o;
	}
}