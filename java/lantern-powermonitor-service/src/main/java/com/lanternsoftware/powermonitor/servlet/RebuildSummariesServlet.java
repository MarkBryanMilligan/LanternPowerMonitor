package com.lanternsoftware.powermonitor.servlet;

import com.lanternsoftware.powermonitor.context.Globals;
import com.lanternsoftware.powermonitor.datamodel.Account;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/rebuildSummaries/*")
public class RebuildSummariesServlet extends SecureServiceServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		if (_authCode.getAccountId() == 100) {
			String[] path = path(_req);
			if (path.length > 0) {
				Globals.opsExecutor.submit(() -> Globals.dao.rebuildSummaries(DaoSerializer.toInteger(CollectionUtils.get(path, 0))));
			}
			else {
				for (String sId : Globals.dao.getProxy().queryForField(Account.class, null, "_id")) {
					int id = DaoSerializer.toInteger(sId);
					if (id != 0) {
						Globals.opsExecutor.submit(() -> Globals.dao.rebuildSummaries(id));
					}
				}
			}
		}
		else
			_rep.setStatus(401);
	}
}
