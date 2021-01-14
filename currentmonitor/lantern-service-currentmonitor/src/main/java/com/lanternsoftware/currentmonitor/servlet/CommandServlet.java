package com.lanternsoftware.currentmonitor.servlet;

import com.lanternsoftware.datamodel.currentmonitor.AuthCode;
import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.LanternFiles;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.ResourceLoader;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/command")
public class CommandServlet extends SecureServlet {

	@Override
	protected void get(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		File folder = new File(LanternFiles.OPS_PATH + _authCode.getAccountId());
		List<String> commands = new ArrayList<>();
		if (folder.exists() && folder.isDirectory()) {
			for (File command : CollectionUtils.asArrayList(folder.listFiles())) {
				if (command.isDirectory())
					continue;
				String c = command.getName();
				String extension = NullUtils.after(c, ".");
				if (NullUtils.isNotEmpty(extension))
					c = c.replace("." + extension, "");
				commands.add(c);
			}
		}
		zipBsonResponse(_rep, new DaoEntity("commands", commands));
	}

	@Override
	protected void post(AuthCode _authCode, HttpServletRequest _req, HttpServletResponse _rep) {
		DaoEntity payload = getRequestZipBson(_req);
		if (payload == null)
			return;
		String command = DaoSerializer.getString(payload, "command");
		String path = LanternFiles.OPS_PATH + _authCode.getAccountId() + File.separator + "payload" + File.separator;
		new File(path).mkdirs();
		ResourceLoader.writeFile(path+ command + ".txt", DaoSerializer.getString(payload, "payload"));
	}
}
