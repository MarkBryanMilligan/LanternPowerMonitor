package com.lanternsoftware.zwave.servlet;

import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.datamodel.zwave.Switch;
import com.lanternsoftware.datamodel.zwave.SwitchSchedule;
import com.lanternsoftware.datamodel.zwave.ThermostatMode;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.zwave.context.Globals;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@WebServlet("/switch/*")
public class SwitchServlet extends SecureServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		String[] path = path(_req);
		int nodeId = NullUtils.toInteger(CollectionUtils.get(path, 0));
		if (path.length == 1) {
			setResponseEntity(_rep, "application/json", "{level:" + Globals.app.getSwitchLevel(nodeId) + "}");
		} else {
			if (nodeId > 0) {
				String command = CollectionUtils.get(path, 1);
				if ("hold".equals(command))
					Globals.app.setSwitchHold(nodeId, true);
				else if ("run".equals(command))
					Globals.app.setSwitchHold(nodeId, false);
				else if ("mode".equals(command))
					Globals.app.setThermostatMode(nodeId, ThermostatMode.fromByte(Byte.parseByte(CollectionUtils.get(path, 2))));
				else {
					Globals.app.setSwitchLevel(nodeId, NullUtils.toInteger(command));
				}
			}
		}
	}

	@Override
	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		String[] path = path(_req);
		int nodeId = NullUtils.toInteger(CollectionUtils.get(path, 0));
		if (path.length > 1) {
			String command = CollectionUtils.get(path, 1);
			if ("schedule".equals(command)) {
				String json = getRequestPayloadAsString(_req);
				List<SwitchSchedule> transitions = DaoSerializer.parseList(json, SwitchSchedule.class);
				Globals.app.setSwitchSchedule(nodeId, transitions);
			}
		}
		else {
			Globals.app.updateSwitch(getRequestPayload(_req, Switch.class));
		}
	}
}
