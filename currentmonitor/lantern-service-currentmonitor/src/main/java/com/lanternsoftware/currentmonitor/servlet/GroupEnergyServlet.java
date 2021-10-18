package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.datamodel.currentmonitor.*;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.DateUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.dao.auth.AuthCode;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.util.*;

@WebServlet("/energy/group/*")
public class GroupEnergyServlet extends SecureServlet {
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
		EnergySummary summary = Globals.dao.getEnergySummary(accountId, path[0], viewMode, start);
		if (summary == null)
			_rep.setStatus(404);
		else {
			BreakerConfig config = Globals.dao.getConfig(accountId);
			Account acct = Globals.dao.getAccount(accountId);
			TimeZone tz = DateUtils.fromTimeZoneId(acct.getTimezone(), "America/Chicago");
			List<BillingRate> rates = CollectionUtils.filter(config.getBillingRates(), _r->_r.isApplicableForDay(start, tz));
			Map<String, Integer> breakerGroupMeters = new HashMap<>();
			for (BreakerGroup group : config.getAllBreakerGroups()) {
				Breaker b = CollectionUtils.getFirst(group.getBreakers());
				if (b != null)
					breakerGroupMeters.put(group.getId(), b.getMeter());
			}
			BreakerGroupEnergy energy = new BreakerGroupEnergy(summary, rates, breakerGroupMeters);
			energy.setToGrid(-summary.flow(null, true, GridFlow.TO));
			energy.setFromGrid(summary.flow(null, true, GridFlow.FROM));
			setResponseEntity(_rep, 200, MediaType.APPLICATION_OCTET_STREAM, DaoSerializer.toZipBson(energy));
		}
	}
}
