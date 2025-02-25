package com.lanternsoftware.powermonitor.datamodel.dao;

import com.lanternsoftware.powermonitor.datamodel.EmailCredentials;
import com.lanternsoftware.powermonitor.datamodel.EmailProvider;
import com.lanternsoftware.util.dao.AbstractDaoSerializer;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoProxyType;
import com.lanternsoftware.util.dao.DaoSerializer;
import java.util.Collections;
import java.util.List;

public class EmailCredentialsSerializer extends AbstractDaoSerializer<EmailCredentials>
{
	@Override
	public Class<EmailCredentials> getSupportedClass()
	{
		return EmailCredentials.class;
	}

	@Override
	public List<DaoProxyType> getSupportedProxies() {
		return Collections.singletonList(DaoProxyType.MONGO);
	}

	@Override
	public DaoEntity toDaoEntity(EmailCredentials _o)
	{
		DaoEntity d = new DaoEntity();
		d.put("provider", DaoSerializer.toEnumName(_o.getProvider()));
		d.put("api_key", _o.getApiKey());
		d.put("api_secret", _o.getApiSecret());
		d.put("email_from", _o.getEmailFrom());
		d.put("server_url_base", _o.getServerUrlBase());
		return d;
	}

	@Override
	public EmailCredentials fromDaoEntity(DaoEntity _d)
	{
		EmailCredentials o = new EmailCredentials();
		o.setProvider(DaoSerializer.getEnum(_d, "provider", EmailProvider.class));
		o.setApiKey(DaoSerializer.getString(_d, "api_key"));
		o.setApiSecret(DaoSerializer.getString(_d, "api_secret"));
		o.setEmailFrom(DaoSerializer.getString(_d, "email_from"));
		o.setServerUrlBase(DaoSerializer.getString(_d, "server_url_base"));
		return o;
	}
}