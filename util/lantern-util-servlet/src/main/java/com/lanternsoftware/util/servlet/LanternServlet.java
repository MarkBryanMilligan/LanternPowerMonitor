package com.lanternsoftware.util.servlet;

import com.lanternsoftware.util.CollectionUtils;
import com.lanternsoftware.util.NullUtils;
import com.lanternsoftware.util.dao.DaoEntity;
import com.lanternsoftware.util.dao.DaoSerializer;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class LanternServlet extends HttpServlet {
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

	public void redirect(HttpServletResponse _response, String _sURL) {
		try {
			_response.sendRedirect(_response.encodeRedirectURL(_sURL));
		}
		catch (IOException _e) {
			_response.setStatus(500);
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
