package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.datamodel.currentmonitor.BreakerPower;
import com.lanternsoftware.datamodel.currentmonitor.HubPowerMinute;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@WebServlet("/power/*")
public class PowerServlet extends SecureServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		String[] path = path(_req);
		if (path.length < 2) {
			_rep.setStatus(400);
			return;
		}
		int hub = DaoSerializer.toInteger(CollectionUtils.get(path, 0));
		int port = DaoSerializer.toInteger(CollectionUtils.get(path, 1));
		jsonResponse(_rep, Globals.dao.getLatestBreakerPower(_authCode.getAccountId(), hub, port));
	}

	@Override
	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		String[] path = path(_req);
		if ((path.length > 0) && NullUtils.isEqual(CollectionUtils.get(path, 0), "hub")) {
			HubPowerMinute m = getRequestPayload(_req, HubPowerMinute.class);
			m.setAccountId(_authCode.getAccountId());
			Globals.dao.putHubPowerMinute(m);
			return;
		}
		if ((path.length > 0) && NullUtils.isEqual(CollectionUtils.get(path, 0), "batch")) {
			List<BreakerPower> powers = DaoSerializer.getList(getRequestZipBson(_req), "readings", BreakerPower.class);
			if (!powers.isEmpty()) {
				CollectionUtils.edit(powers, _p->_p.setAccountId(_authCode.getAccountId()));
				Globals.dao.getProxy().save(powers);
			}
			return;
		}
		BreakerPower power = getRequestPayload(_req, BreakerPower.class);
		power.setAccountId(_authCode.getAccountId());
		Globals.dao.putBreakerPower(power);
	}
}
