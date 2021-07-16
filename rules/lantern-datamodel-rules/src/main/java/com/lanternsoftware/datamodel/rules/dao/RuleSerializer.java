package com.lanternsoftware.datamodel.rules.dao;

import com.lanternsoftware.datamodel.rules.Action;
import com.lanternsoftware.datamodel.rules.Criteria;
import com.lanternsoftware.datamodel.rules.Rule;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class RuleSerializer extends AbstractDaoSerializer<Rule>
{
	@Override
	public Class<Rule> getSupportedClass()
	{
		return Rule.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(Rule _o)
	{
		DaoEntity d = new DaoEntity();
		if (_o.getId() != null)
			d.put("_id", _o.getId());
		d.put("account_id", _o.getAccountId());
		d.put("or", _o.isOr());
		d.put("criteria", DaoSerializer.toDaoEntities(_o.getCriteria(), DaoProxyType.MONGO));
		d.put("actions", DaoSerializer.toDaoEntities(_o.getActions(), DaoProxyType.MONGO));
		return d;
	}

	@Override
	public Rule fromDaoEntity(DaoEntity _d)
	{
		Rule o = new Rule();
		o.setId(DaoSerializer.getString(_d, "_id"));
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setOr(DaoSerializer.getBoolean(_d, "or"));
		o.setCriteria(DaoSerializer.getList(_d, "criteria", Criteria.class));
		o.setActions(DaoSerializer.getList(_d, "actions", Action.class));
		return o;
	}
}