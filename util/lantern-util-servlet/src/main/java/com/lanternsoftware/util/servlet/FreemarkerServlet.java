package com.lanternsoftware.util.servlet;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import freemarker.template.Configuration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
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
		String linkPrefix = "";
		String[] path = getPath(_req);
		if (path.length > 1) {
			StringBuilder prefix = new StringBuilder();
			for(int i=0; i<path.length-1; i++) {
				prefix.append("../");
			}
			linkPrefix = prefix.toString();
		}
		model.put("link_prefix", linkPrefix);
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

	protected void ajaxRender(HttpServletResponse _rep, String _template, Map<String, Object> _templateModel) {
		ajaxRender(_rep, _template, _templateModel, null);
	}

	protected void ajaxRender(HttpServletResponse _rep, String _templateName, Map<String, Object> _templateModel, Map<String, Object> _jsonRep) {
		ajaxHtml(_rep, FreemarkerUtil.render(getFreemarkerConfig(), _templateName, _templateModel), _jsonRep);

	}

	protected static void ajaxHtml(HttpServletResponse _rep, String _html) {
		ajaxHtml(_rep, _html, null);
	}

	protected static void ajaxHtml(HttpServletResponse _rep, String _html, Map<String, Object> _model) {
		if (_model == null) {
			_model = new HashMap<>();
		}
		_model.put("html", _html);
		ajaxJson(_rep, _model);
	}

	protected static void ajaxJson(HttpServletResponse _rep, Map<String, Object> _model) {
		DaoEntity json = new DaoEntity(_model);
		setResponseEntity(_rep, "application/json", DaoSerializer.toJson(json));
	}

	protected void ajaxRedirect(HttpServletResponse _rep, String _url) {
		setResponseEntity(_rep, "application/json", DaoSerializer.toJson(new DaoEntity("redirect", _url)));
	}

	protected void ajaxError(HttpServletResponse _rep, String _error) {
		ajaxError(_rep, _error, null);
	}

	protected void ajaxError(HttpServletResponse _rep, String _error, DaoEntity _model) {
		if (_model == null) {
			_model = new DaoEntity();
		}
		_model.put("error", _error);
		setResponseEntity(_rep, "application/json", DaoSerializer.toJson(_model, false, false));
	}
}
