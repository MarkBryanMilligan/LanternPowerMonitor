package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.datamodel.currentmonitor.Account;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/rebuildSummaries/*")
public class RebuildSummariesServlet extends SecureServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		if (_authCode.getAccountId() == 100) {
			String[] path = path(_req);
			if (path.length > 0) {
				Globals.dao.rebuildSummariesAsync(DaoSerializer.toInteger(CollectionUtils.get(path, 0)));
			}
			else {
				for (String sId : Globals.dao.getProxy().queryForField(Account.class, null, "_id")) {
					int id = DaoSerializer.toInteger(sId);
					if (id != 0)
						Globals.dao.rebuildSummariesAsync(id);
				}
			}
		}
		else
			_rep.setStatus(401);
	}
}
