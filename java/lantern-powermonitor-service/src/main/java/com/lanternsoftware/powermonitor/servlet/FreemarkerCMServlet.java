package com.lanternsoftware.powermonitor.servlet;

import com.lanternsoftware.util.servlet.FreemarkerConfigUtil;
import com.lanternsoftware.util.servlet.FreemarkerServlet;
import com.lanternsoftware.util.servlet.FreemarkerUtil;
import freemarker.template.Configuration;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public abstract class FreemarkerCMServlet extends FreemarkerServlet {
	protected static final Configuration CONFIG = FreemarkerConfigUtil.createConfig(FreemarkerCMServlet.class, "/templates", 100);

	@Override
	protected Configuration getFreemarkerConfig() {
		return CONFIG;
	}

	public void renderBody(HttpServletResponse _rep, String _sHtmlResourceKey, Map<String, Object> _mapModel) {
		_mapModel.put("body", FreemarkerUtil.render(getFreemarkerConfig(), _sHtmlResourceKey, _mapModel));
		render(_rep, "frame.ftl", _mapModel);
	}
}
