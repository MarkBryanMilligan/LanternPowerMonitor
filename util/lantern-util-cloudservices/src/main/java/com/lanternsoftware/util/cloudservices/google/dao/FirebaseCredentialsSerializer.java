package com.lanternsoftware.util.cloudservices.google.dao;

import com.lanternsoftware.util.cloudservices.google.FirebaseCredentials;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;

import java.util.Collections;
import java.util.List;

public class FirebaseCredentialsSerializer extends AbstractDaoSerializer<FirebaseCredentials>
{
	@Override
	public Class<FirebaseCredentials> getSupportedClass()
	{
		return FirebaseCredentials.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(FirebaseCredentials _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("type", _o.getType());
		d.put("project_id", _o.getProjectId());
		d.put("private_key_id", _o.getPrivateKeyId());
		d.put("private_key", _o.getPrivateKey());
		d.put("client_email", _o.getClientEmail());
		d.put("client_id", _o.getClientId());
		d.put("auth_uri", _o.getAuthUri());
		d.put("token_uri", _o.getTokenUri());
		d.put("auth_provider_x509_cert_url", _o.getAuthProviderX509CertUrl());
		d.put("client_x509_cert_url", _o.getClientX509CertUrl());
		return d;
	}

	@Override
	public FirebaseCredentials fromDaoEntity(DaoEntity _d)
	{
		FirebaseCredentials o = new FirebaseCredentials();
		o.setType(DaoSerializer.getString(_d, "type"));
		o.setProjectId(DaoSerializer.getString(_d, "project_id"));
		o.setPrivateKeyId(DaoSerializer.getString(_d, "private_key_id"));
		o.setPrivateKey(DaoSerializer.getString(_d, "private_key"));
		o.setClientEmail(DaoSerializer.getString(_d, "client_email"));
		o.setClientId(DaoSerializer.getString(_d, "client_id"));
		o.setAuthUri(DaoSerializer.getString(_d, "auth_uri"));
		o.setTokenUri(DaoSerializer.getString(_d, "token_uri"));
		o.setAuthProviderX509CertUrl(DaoSerializer.getString(_d, "auth_provider_x509_cert_url"));
		o.setClientX509CertUrl(DaoSerializer.getString(_d, "client_x509_cert_url"));
		return o;
	}
}