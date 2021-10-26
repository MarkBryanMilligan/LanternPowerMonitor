package com.lanternsoftware.thermometer.servlet;

import com.lanternsoftware.thermometer.IThermometer;
import com.lanternsoftware.thermometer.context.Globals;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.servlet.LanternServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/temp/*")
public class TempServlet extends LanternServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		int idx = DaoSerializer.toInteger(CollectionUtils.get(path(req), 0));
		IThermometer therm = CollectionUtils.get(Globals.thermometers, idx);
		double temp = therm == null ? -273 : therm.getTemperatureCelsius();
		setResponseEntity(resp, "application/json", "{\"temp\": "+ temp + "}");
	}
}
