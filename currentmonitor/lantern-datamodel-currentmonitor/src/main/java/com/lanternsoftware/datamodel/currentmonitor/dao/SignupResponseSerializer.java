package com.lanternsoftware.datamodel.currentmonitor.dao;

import com.lanternsoftware.datamodel.currentmonitor.SignupResponse;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class SignupResponseSerializer extends AbstractDaoSerializer<SignupResponse>
{
	@Override
	public Class<SignupResponse> getSupportedClass()
	{
		return SignupResponse.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(SignupResponse _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("error", _o.getError());
		d.put("auth_code", _o.getAuthCode());
		return d;
	}

	@Override
	public SignupResponse fromDaoEntity(DaoEntity _d)
	{
		SignupResponse o = new SignupResponse();
		o.setError(DaoSerializer.getString(_d, "error"));
		o.setAuthCode(DaoSerializer.getString(_d, "auth_code"));
		return o;
	}
}