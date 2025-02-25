package com.lanternsoftware.thermometer.servlet;

import com.lanternsoftware.thermometer.context.Globals;
import com.lanternsoftware.util.servlet.LanternServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/co2")
public class CO2Servlet extends LanternServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		if (Globals.co2Sensor == null)
			resp.setStatus(404);
		else
			setResponseEntity(resp, "application/json", "{\"ppm\": "+ Globals.co2Sensor.getPPM() + "}");
	}
}
