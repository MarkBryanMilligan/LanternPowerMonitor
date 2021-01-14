package com.lanternsoftware.util.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lanternsoftware.util.NullUtils;

public class HttpPool {
    private static final Logger LOG = LoggerFactory.getLogger(HttpPool.class);

    private final RequestConfig requestConfig;
    private final CookieStore cookieStore = new BasicCookieStore();
    private final ConnectionKeepAliveStrategy keepAliveStrategy;
    private final PoolingHttpClientConnectionManager connectionManager;

    public HttpPool(int _maxTotalConnections, int _maxPerRoute) {
        this(_maxTotalConnections, _maxPerRoute, 10000, 5000, 10000);
    }

    public HttpPool(int _maxTotalConnections, int _maxPerRoute, int _socketTimeoutMs, int _connectTimeoutMs, int _connectionRequestTimeoutMs) {
        requestConfig = RequestConfig.custom().setSocketTimeout(_socketTimeoutMs).setConnectTimeout(_connectTimeoutMs).setConnectionRequestTimeout(_connectionRequestTimeoutMs).setCookieSpec(CookieSpecs.STANDARD).build();
        keepAliveStrategy = (HttpResponse response, HttpContext context) -> 0;
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(_maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(_maxPerRoute);
    }

    public void shutdown() {
        connectionManager.shutdown();
    }

    public CloseableHttpResponse execute(HttpUriRequest _request) {
        try {
            return getClient().execute(_request);
        }
        catch (IOException _e) {
            LOG.error("Failed to make http request to " + _request.getURI().toString(), _e);
            return null;
        }
    }

    public CloseableHttpResponse execute(HttpUriRequest _request, HttpContext _context) {
        try {
            return getClient().execute(_request, _context);
        }
        catch (IOException _e) {
            LOG.error("Failed to make http request to " + _request.getURI().toString(), _e);
            return null;
        }
    }

    private CloseableHttpClient getClient() {
        return HttpClients.custom().setConnectionManager(connectionManager).setDefaultCookieStore(cookieStore).setDefaultRequestConfig(requestConfig).setKeepAliveStrategy(keepAliveStrategy).build();
    }

    public String executeToString(HttpUriRequest _request) {
        return NullUtils.toString(executeToByteArray(_request));
    }

    public byte[] executeToByteArray(HttpUriRequest _request) {
        InputStream is = null;
        CloseableHttpResponse resp = null;
        try {
            resp = execute(_request);
            if ((resp.getStatusLine().getStatusCode() < 200) || (resp.getStatusLine().getStatusCode() >= 300)) {
                LOG.error("Failed to make http request to " + _request.getURI().toString() + ". Status code: " + resp.getStatusLine().getStatusCode());
                return null;
            }
            HttpEntity entity = resp.getEntity();
            if (entity != null) {
                is = entity.getContent();
                return IOUtils.toByteArray(is);
            }
            return null;
        }
        catch (IOException _e) {
            LOG.error("Failed to make http request to " + _request.getURI().toString(), _e);
            return null;
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(resp);
        }
    }

    public static void addBasicAuthHeader(HttpUriRequest _request, String _username, String _password) {
        _request.addHeader("Authorization", "Basic " + Base64.encodeBase64String(NullUtils.toByteArray(_username + ":" + _password)));
    }
}
