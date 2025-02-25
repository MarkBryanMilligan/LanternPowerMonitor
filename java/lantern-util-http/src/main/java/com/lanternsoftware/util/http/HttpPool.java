package com.lanternsoftware.util.http;

import com.lanternsoftware.util.NullUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

public class HttpPool {
    private static final Logger LOG = LoggerFactory.getLogger(HttpPool.class);

    private final RequestConfig requestConfig;
    private final CookieStore cookieStore = new BasicCookieStore();
    private final ConnectionKeepAliveStrategy keepAliveStrategy;
    private final PoolingHttpClientConnectionManager connectionManager;

    public HttpPool(int _maxTotalConnections, int _maxPerRoute) {
        this(_maxTotalConnections, _maxPerRoute, null, null);
    }

    public HttpPool(int _maxTotalConnections, int _maxPerRoute, KeyStore _keystore, String _keystorePassword) {
        this(_maxTotalConnections, _maxPerRoute, 10000, 5000, 10000, _keystore, _keystorePassword, true);
    }

    public HttpPool(int _maxTotalConnections, int _maxPerRoute, int _socketTimeoutMs, int _connectTimeoutMs, int _connectionRequestTimeoutMs) {
        this(_maxTotalConnections, _maxPerRoute, _socketTimeoutMs, _connectTimeoutMs, _connectionRequestTimeoutMs, null, null, true);
    }

    public HttpPool(int _maxTotalConnections, int _maxPerRoute, int _socketTimeoutMs, int _connectTimeoutMs, int _connectionRequestTimeoutMs, KeyStore _keystore, String _keystorePassword, boolean _validateSSLCertificates) {
        requestConfig = RequestConfig.custom().setSocketTimeout(_socketTimeoutMs).setConnectTimeout(_connectTimeoutMs).setConnectionRequestTimeout(_connectionRequestTimeoutMs).setCookieSpec(CookieSpecs.STANDARD).build();
        keepAliveStrategy = (HttpResponse response, HttpContext context) -> 0;
        Registry<ConnectionSocketFactory> registry = null;
        if ((_keystore != null) || !_validateSSLCertificates) {
            try {
                SSLContextBuilder contextBuilder = SSLContexts.custom();
                if (_keystore != null)
                    contextBuilder.loadKeyMaterial(_keystore, _keystorePassword.toCharArray());
                if (!_validateSSLCertificates)
                    contextBuilder.loadTrustMaterial(null, (x509CertChain, authType) -> true);
                SSLConnectionSocketFactory socketFactory = _validateSSLCertificates ? new SSLConnectionSocketFactory(contextBuilder.build()) : new SSLConnectionSocketFactory(contextBuilder.build(), NoopHostnameVerifier.INSTANCE);
                registry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", socketFactory).register("http", new PlainConnectionSocketFactory()).build();
            } catch (Exception _e) {
                LOG.error("Failed to load SSL keystore", _e);
            }
        }
        connectionManager = registry != null ? new PoolingHttpClientConnectionManager(registry) : new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(_maxTotalConnections);
        connectionManager.setDefaultMaxPerRoute(_maxPerRoute);
    }

    public static Builder builder() {
        return new Builder();
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
        return executeToPayload(_request).getPayload();
    }

    public HttpResponsePayload executeToPayload(HttpUriRequest _request) {
        InputStream is = null;
        CloseableHttpResponse resp = null;
        try {
            resp = execute(_request);
            if (resp == null)
                return new HttpResponsePayload(HttpStatus.SC_INTERNAL_SERVER_ERROR, null);
            if ((resp.getStatusLine().getStatusCode() < 200) || (resp.getStatusLine().getStatusCode() >= 300))
                LOG.error("Failed to make http request to " + _request.getURI().toString() + ". Status code: " + resp.getStatusLine().getStatusCode());
            HttpEntity entity = resp.getEntity();
            if (entity != null) {
                is = entity.getContent();
                return new HttpResponsePayload(resp.getStatusLine().getStatusCode(), IOUtils.toByteArray(is));
            }
            return new HttpResponsePayload(resp.getStatusLine().getStatusCode(), null);
        }
        catch (Exception _e) {
            LOG.error("Failed to make http request to " + _request.getURI().toString(), _e);
            return new HttpResponsePayload(HttpStatus.SC_INTERNAL_SERVER_ERROR, null);
        }
        finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(resp);
        }
    }

    public static void addBasicAuthHeader(HttpUriRequest _request, String _username, String _password) {
        _request.addHeader("Authorization", "Basic " + Base64.encodeBase64String(NullUtils.toByteArray(_username + ":" + _password)));
    }

    public static final class Builder {
        private int maxTotalConnections = 10;
        private int maxPerRoute = 10;
        private int socketTimeoutMs = 10000;
        private int connectTimeoutMs = 5000;
        private int connectionRequestTimeoutMs = 10000;
        private KeyStore keystore;
        private String keystorePassword;
        private boolean validateSSLCertificates = true;

        private Builder() {
        }

        public Builder withMaxTotalConnections(int val) {
            maxTotalConnections = val;
            return this;
        }

        public Builder withMaxPerRoute(int val) {
            maxPerRoute = val;
            return this;
        }

        public Builder withSocketTimeoutMs(int val) {
            socketTimeoutMs = val;
            return this;
        }

        public Builder withConnectTimeoutMs(int val) {
            connectTimeoutMs = val;
            return this;
        }

        public Builder withConnectionRequestTimeoutMs(int val) {
            connectionRequestTimeoutMs = val;
            return this;
        }

        public Builder withKeystore(KeyStore _keystore, String _password) {
            keystore = _keystore;
            keystorePassword = _password;
            return this;
        }

        public Builder withValidateSSLCertificates(boolean val) {
            validateSSLCertificates = val;
            return this;
        }
        public HttpPool build() {
            return new HttpPool(maxTotalConnections, maxPerRoute, socketTimeoutMs, connectTimeoutMs, connectionRequestTimeoutMs, keystore, keystorePassword, validateSSLCertificates);
        }
    }
}
