package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.datamodel.currentmonitor.BreakerConfig;
import com.lanternsoftware.datamodel.currentmonitor.bom.BOM;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.csv.CSVWriter;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoQuery;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.servlet.LanternServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/bom/*")
public class BomServlet extends LanternServlet {
	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		String[] path = path(_req);
		if (CollectionUtils.size(path) < 1){
			_rep.setStatus(401);
			return;
		}
		DaoEntity id = CollectionUtils.getFirst(Globals.dao.getProxy().queryForEntities("bom", new DaoQuery("_id", path[0])));
		int acctId = DaoSerializer.getInteger(id, "acct_id");
		if (acctId == 0) {
			_rep.setStatus(401);
			return;
		}
		BreakerConfig config = Globals.dao.getConfig(acctId);
		if (config == null) {
			_rep.setStatus(401);
			return;
		}
		setResponseEntity(_rep, "text/csv",CSVWriter.toByteArray(BOM.fromConfig(config).toCsv(false)));
	}
}
