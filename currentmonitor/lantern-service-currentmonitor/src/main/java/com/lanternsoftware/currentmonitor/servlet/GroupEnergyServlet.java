package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.currentmonitor.context.Globals;
import com.lanternsoftware.util.dao.auth.AuthCode;
import com.lanternsoftware.datamodel.currentmonitor.BreakerGroupEnergy;
import com.lanternsoftware.datamodel.currentmonitor.EnergyBlockViewMode;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@WebServlet("/energy/group/*")
public class GroupEnergyServlet extends SecureServlet {
	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		String[] path = path(_req);
		if (path.length < 3) {
			_rep.setStatus(400);
			return;
		}
		EnergyBlockViewMode viewMode = NullUtils.toEnum(EnergyBlockViewMode.class, path[1], EnergyBlockViewMode.DAY);
		Date start = new Date(NullUtils.toLong(path[2]));
		List<BreakerGroupEnergy> energies = CollectionUtils.transform(_authCode.getAllAccountIds(), _id->Globals.dao.getBreakerGroupEnergy(_id, path[0], viewMode, start), true);
		if (CollectionUtils.isNotEmpty(energies)) {
			BreakerGroupEnergy energy;
			if (energies.size() > 1) {
				energy = new BreakerGroupEnergy();
				energy.setAccountId(_authCode.getAccountId());
				energy.setGroupId("Sites");
				energy.setGroupName("Sites");
				energy.setStart(start);
				energy.setViewMode(viewMode);
				energy.setSubGroups(CollectionUtils.asArrayList(energies));
			}
			else
				energy = CollectionUtils.getFirst(energies);
			if (NullUtils.isEqual(CollectionUtils.get(path, 3), "bin"))
				zipBsonResponse(_rep, energy);
			else
				jsonResponse(_rep, energy);
		} else
			_rep.setStatus(404);
	}
}
