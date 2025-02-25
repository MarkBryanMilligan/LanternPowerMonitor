package com.lanternsoftware.powermonitor.datamodel.dao;

import com.lanternsoftware.powermonitor.datamodel.Account;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.util.Collections;
import java.util.List;

public class AccountSerializer extends AbstractDaoSerializer<Account>
{
	@Override
	public Class<Account> getSupportedClass()
	{
		return Account.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(Account _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("_id", String.valueOf(_o.getId()));
		d.put("username", _o.getUsername());
		d.put("password", _o.getPassword());
		d.put("timezone", _o.getTimezone());
		if (CollectionUtils.isNotEmpty(_o.getAuxiliaryAccountIds()))
			d.put("aux_account_ids", CollectionUtils.toByteArray(_o.getAuxiliaryAccountIds()));
		return d;
	}

	@Override
	public Account fromDaoEntity(DaoEntity _d)
	{
		Account o = new Account();
		o.setId(DaoSerializer.getInteger(_d, "_id"));
		o.setUsername(DaoSerializer.getString(_d, "username"));
		o.setPassword(DaoSerializer.getString(_d, "password"));
		o.setTimezone(DaoSerializer.getString(_d, "timezone"));
		o.setAuxiliaryAccountIds(CollectionUtils.fromByteArrayOfIntegers(DaoSerializer.getByteArray(_d, "aux_account_ids")));
		return o;
	}
}