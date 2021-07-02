package com.lanternsoftware.util.servlet;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import freemarker.template.Configuration;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class FreemarkerServlet extends HttpServlet {
    protected abstract Configuration getFreemarkerConfig();
    
    public static String[] getPath(HttpServletRequest _request) {
        String sPath = _request.getRequestURI().substring(_request.getContextPath().length());
        if (sPath.startsWith("/"))
            sPath = sPath.substring(1);
        String[] path = sPath.split("/");
        if ((path.length == 0) || (path[0].length() == 0))
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

    public static void setResponseHtml(HttpServletResponse _response, String _sHtml) {
        setResponseEntity(_response, MediaType.TEXT_HTML, _sHtml);
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
            } else
                _response.setContentLength(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    protected void zipBsonResponse(HttpServletResponse _response, Object _object)
    {
        setResponseEntity(_response, 200, MediaType.APPLICATION_OCTET_STREAM, DaoSerializer.toZipBson(_object));
    }

    protected void jsonResponse(HttpServletResponse _response, Object _object)
    {
        setResponseEntity(_response, 200, MediaType.APPLICATION_JSON, DaoSerializer.toJson(_object));
    }

    protected void jsonResponse(HttpServletResponse _response, String _json)
    {
        setResponseEntity(_response, 200, MediaType.APPLICATION_JSON, _json);
    }

    protected String getRequestPayloadAsString(HttpServletRequest _req) {
        return NullUtils.toString(getRequestPayload(_req));
    }

    protected byte[] getRequestPayload(HttpServletRequest _req) {
        InputStream is = null;
        try {
            is = _req.getInputStream();
            return IOUtils.toByteArray(is);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }

    protected DaoEntity getRequestZipBson(HttpServletRequest _req) {
        return DaoSerializer.fromZipBson(getRequestPayload(_req));
    }

    protected <T> T getRequestPayload(HttpServletRequest _req, Class<T> _retClass) {
        return DaoSerializer.fromZipBson(getRequestPayload(_req), _retClass);
    }

    protected String[] path(HttpServletRequest _req) {
        return NullUtils.cleanSplit(NullUtils.makeNotNull(_req.getPathInfo()), "/");
    }

    protected boolean isPath(HttpServletRequest _req, int _index, String _path) {
        return NullUtils.isEqual(_path, CollectionUtils.get(path(_req), _index));
    }

}
