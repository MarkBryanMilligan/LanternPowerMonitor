package com.lanternsoftware.thermometer.servlet;

import com.lanternsoftware.util.NullUtils;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

public abstract class ThermoServlet extends HttpServlet {
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
			} else
				_response.setContentLength(0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(os);
		}
	}
}
