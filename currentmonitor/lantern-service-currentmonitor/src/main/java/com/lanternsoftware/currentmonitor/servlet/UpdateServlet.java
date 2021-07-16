package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoSerializer;
import com.lanternsoftware.util.servlet.LanternServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.File;

@WebServlet("/update/*")
public class UpdateServlet extends LanternServlet {
	@Override
	protected void doGet(HttpServletRequest _req, HttpServletResponse _rep) {
		if (isPath(_req, 0, "version"))
			setResponseEntity(_rep, MediaType.APPLICATION_OCTET_STREAM, DaoSerializer.toZipBson(DaoSerializer.parse(ResourceLoader.loadFileAsString(LanternFiles.OPS_PATH + "release" + File.separator + "version.json"))));
		else
			setResponseEntity(_rep, MediaType.APPLICATION_OCTET_STREAM, ResourceLoader.loadFile(LanternFiles.OPS_PATH + "release" + File.separator + "lantern-currentmonitor.jar"));
	}
}
