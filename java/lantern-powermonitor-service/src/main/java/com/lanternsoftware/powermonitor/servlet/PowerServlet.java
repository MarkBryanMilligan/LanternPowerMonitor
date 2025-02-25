package com.lanternsoftware.powermonitor.servlet;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.powermonitor.dataaccess.MongoPowerMonitorDao;
import com.lanternsoftware.powermonitor.datamodel.HubCommands;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.powermonitor.datamodel.BreakerPower;
import com.lanternsoftware.powermonitor.datamodel.HubPowerMinute;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@WebServlet("/power/*")
public class PowerServlet extends SecureServiceServlet {
	private static final Logger logger = LoggerFactory.getLogger(MongoPowerMonitorDao.class);

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
			if (m == null)
				return;
			logger.info("Hub Power from ip {}, account {}, hub {}", _req.getRemoteAddr(), m.getAccountId(), m.getHub());
			m.setAccountId(_authCode.getAccountId());
			Globals.dao.putHubPowerMinute(m);
			return;
		}
		if ((path.length > 0) && NullUtils.isEqual(CollectionUtils.get(path, 0), "batch")) {
			DaoEntity payload = getRequestZipBson(_req);
			List<BreakerPower> powers = DaoSerializer.getList(payload, "readings", BreakerPower.class);
			if (!powers.isEmpty()) {
				CollectionUtils.edit(powers, _p->_p.setAccountId(_authCode.getAccountId()));
				Globals.dao.getProxy().save(powers);
				int hub = DaoSerializer.getInteger(payload, "hub");
				HubCommands commands = Globals.getCommandsForHub(_authCode.getAccountId(), hub);
				if (commands != null)
					zipBsonResponse(_rep, commands);
			}
			return;
		}
		BreakerPower power = getRequestPayload(_req, BreakerPower.class);
		if (power == null)
			return;
		power.setAccountId(_authCode.getAccountId());
		Globals.dao.putBreakerPower(power);
	}
}
