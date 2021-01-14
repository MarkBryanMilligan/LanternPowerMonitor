package com.lanternsoftware.util.servlet;

import java.io.StringWriter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;

public abstract class FreemarkerUtil {
    protected static final Logger LOG = LoggerFactory.getLogger(FreemarkerUtil.class);

    public static String render(Configuration _config, String _templateName, Map<String, Object>_model) {
        try {
            Template temp = _config.getTemplate(_templateName);
            StringWriter writer = new StringWriter();
            temp.process(_model, writer);
            writer.close();
            return writer.toString();
        }
        catch (Exception e) {
            LOG.error("Failed to render html", e);
            return null;
        }
    }
}
