package com.lanternsoftware.util.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;

import freemarker.template.Configuration;

public abstract class FreemarkerServlet extends HttpServlet {
    protected static final Logger LOG = LoggerFactory.getLogger(FreemarkerServlet.class);
    
    protected abstract Configuration getFreemarkerConfig();
    
    public static String[] getPath(HttpServletRequest _request) {
        String sPath = _request.getRequestURI().substring(_request.getContextPath().length());
        if (sPath.startsWith("/"))
            sPath = sPath.substring(1);
        String[] path = sPath.split("/");
        if ((path == null) || (path.length == 0) || (path[0].length() == 0))
            return new String[] { "index" };
        int iExtPos = CollectionUtils.last(path).lastIndexOf(".");
        if (iExtPos > -1) {
            path[path.length - 1] = CollectionUtils.last(path).substring(0, iExtPos);
        }
        return path;
    }
    
    public static void redirect(HttpServletResponse _response, String _sURL) throws IOException {
        _response.sendRedirect(_response.encodeRedirectURL(_sURL));
    }
    
    public static void setResponseHtml(HttpServletResponse _response, String _sHtml) {
        setResponseEntity(_response, "text/html", _sHtml);
    }
    
    public static void setResponseEntity(HttpServletResponse _response, String _sContentType, String _sEntity) {
        setResponseEntity(_response, 200, _sContentType, _sEntity);
    }
    
    public static void setResponseEntity(HttpServletResponse _response, String _sContentType, byte[] _btData) {
        setResponseEntity(_response, 200, _sContentType, _btData);
    }
    
    public static void setResponseEntity(HttpServletResponse _response, int _iStatus, String _sContentType, String _sEntity) {
        setResponseEntity(_response, _iStatus, _sContentType, NullUtils.toByteArray(_sEntity));
    }
    
    public static void setResponseEntity(HttpServletResponse _response, int _iStatus, String _sContentType, byte[] _btData) {
        OutputStream os = null;
        try {
            _response.setStatus(_iStatus);
            _response.setCharacterEncoding("UTF-8");
            _response.setContentType(_sContentType);
            if ((_btData != null) && (_btData.length > 0)) {
                _response.setContentLength(_btData.length);
                os = _response.getOutputStream();
                os.write(_btData);
            }
            else
                _response.setContentLength(0);
        }
        catch (Exception e) {
            if (!e.getClass().getSimpleName().equals("ClientAbortException"))
                LOG.error("Failed to set response entity", e);
        }
        finally {
            IOUtils.closeQuietly(os);
        }
    }
    
    public void render(HttpServletResponse _rep, String _sHtmlResourceKey, Map<String, Object> _mapModel) {
        String html = FreemarkerUtil.render(getFreemarkerConfig(), _sHtmlResourceKey, _mapModel);
        if (html == null)
            _rep.setStatus(500);
        else
            setResponseHtml(_rep, html);
    }
    
    protected Map<String, Object> simpleModel(String _name, Object _value) {
        Map<String, Object> mapModel = new HashMap<String, Object>();
        mapModel.put(_name, _value);
        return mapModel;
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
