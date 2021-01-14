package com.lanternsoftware.thermometer.servlet;

import com.lanternsoftware.thermometer.context.Globals;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet("/temp")
public class TempServlet extends ThermoServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		setResponseEntity(resp, "application/json", "{\"temp\": "+ Globals.app.getTemperature() + "}");
	}
}
