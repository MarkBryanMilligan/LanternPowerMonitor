package com.lanternsoftware.util.servlet;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import freemarker.template.Configuration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public abstract class FreemarkerServlet extends LanternServlet {
	protected abstract Configuration getFreemarkerConfig();

	public static String[] getPath(HttpServletRequest _request) {
		String sPath = _request.getRequestURI().substring(_request.getContextPath().length());
		if (sPath.startsWith("/"))
			sPath = sPath.substring(1);
		String[] path = sPath.split("/");
		if ((path.length == 0) || (path[0].length() == 0))
			return new String[]{"index"};
		int iExtPos = CollectionUtils.last(path).lastIndexOf(".");
		if (iExtPos > -1) {
			path[path.length - 1] = CollectionUtils.last(path).substring(0, iExtPos);
		}
		return path;
	}

	public static void redirect(HttpServletResponse _response, String _sURL) throws IOException {
		_response.sendRedirect(_response.encodeRedirectURL(_sURL));
	}

	public void render(HttpServletResponse _rep, String _sHtmlResourceKey, Map<String, Object> _mapModel) {
		String html = FreemarkerUtil.render(getFreemarkerConfig(), _sHtmlResourceKey, _mapModel);
		if (html == null)
			_rep.setStatus(500);
		else
			setResponseHtml(_rep, html);
	}

	public static DaoEntity model(HttpServletRequest _req, String _name, Object _value) {
		DaoEntity model = model(_req);
		model.put(_name, _value);
		return model;
	}

	protected static DaoEntity model(HttpServletRequest _req) {
		DaoEntity model = new DaoEntity("context", _req.getContextPath());
		model.put("css_version", "1.0.0");
		return model;
	}

	public static <T> T getSessionVar(HttpServletRequest _req, String _name) {
		return (T) _req.getSession().getAttribute(_name);
	}

	public static void putSessionVar(HttpServletRequest _req, String _name, Object _var) {
		_req.getSession().setAttribute(_name, _var);
	}

	protected String relativeOffset(HttpServletRequest _req) {
		String[] path = getPath(_req);
		StringBuilder offset = new StringBuilder();
		for (int i = 1; i < CollectionUtils.size(path); i++) {
			offset.append("../");
		}
		return offset.toString();
	}

	protected Cookie getCookie(HttpServletRequest _req, String _name) {
		if (_req.getCookies() != null) {
			for (Cookie c : _req.getCookies()) {
				if (NullUtils.isEqual(c.getName(), _name))
					return c;
			}
		}
		return null;
	}
}
