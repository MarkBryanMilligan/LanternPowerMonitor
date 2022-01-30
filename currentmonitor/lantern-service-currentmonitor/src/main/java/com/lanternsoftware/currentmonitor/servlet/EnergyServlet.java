package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.datamodel.currentmonitor.EnergyViewMode;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.util.Date;

@WebServlet("/energy/*")
public class EnergyServlet extends SecureServiceServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		String[] path = path(_req);
		if (path.length < 3) {
			_rep.setStatus(400);
			return;
		}
		int accountId = DaoSerializer.toInteger(CollectionUtils.getFirst(_authCode.getAllAccountIds()));
		if (accountId == 0) {
			_rep.setStatus(404);
			return;
		}
		EnergyViewMode viewMode = NullUtils.toEnum(EnergyViewMode.class, path[1], EnergyViewMode.DAY);
		Date start = new Date(NullUtils.toLong(path[2]));
		byte[] energy = Globals.dao.getEnergySummaryBinary(accountId, path[0], viewMode, start);
		if (energy == null)
			_rep.setStatus(404);
		else
			setResponseEntity(_rep, 200, MediaType.APPLICATION_OCTET_STREAM, energy);
	}
}
