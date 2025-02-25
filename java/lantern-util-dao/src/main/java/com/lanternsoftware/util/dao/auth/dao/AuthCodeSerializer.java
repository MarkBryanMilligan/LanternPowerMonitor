package com.lanternsoftware.util.dao.auth.dao;

import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.util.Collections;
import java.util.List;

public class AuthCodeSerializer extends AbstractDaoSerializer<AuthCode>
{
	@Override
	public Class<AuthCode> getSupportedClass()
	{
		return AuthCode.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(AuthCode _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("account_id", _o.getAccountId());
		if (CollectionUtils.isNotEmpty(_o.getAuxiliaryAccountIds()))
			d.put("aux_account_ids", CollectionUtils.toByteArray(_o.getAuxiliaryAccountIds()));
		return d;
	}

	@Override
	public AuthCode fromDaoEntity(DaoEntity _d)
	{
		AuthCode o = new AuthCode();
		o.setAccountId(DaoSerializer.getInteger(_d, "account_id"));
		o.setAuxiliaryAccountIds(CollectionUtils.fromByteArrayOfIntegers(DaoSerializer.getByteArray(_d, "aux_account_ids")));
		return o;
	}
}