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

@WebServlet("/charge/*")
public class ChargeServlet extends SecureServlet {
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
		EnergyViewMode viewMode = NullUtils.toEnum(EnergyViewMode.class, path[2], EnergyViewMode.DAY);
		Date start = new Date(NullUtils.toLong(path[3]));
		byte[] charges = Globals.dao.getChargeSummaryBinary(accountId, DaoSerializer.toInteger(path[0]), path[1], viewMode, start);
		if (charges == null)
			_rep.setStatus(404);
		else
			setResponseEntity(_rep, 200, MediaType.APPLICATION_OCTET_STREAM, charges);
	}
}
