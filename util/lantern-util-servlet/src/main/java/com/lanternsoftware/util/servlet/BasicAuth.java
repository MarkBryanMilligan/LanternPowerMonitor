package com.lanternsoftware.util.servlet;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;

import com.lanternsoftware.util.NullUtils;

public class BasicAuth {
    private final String username;
    private final String password;

    public BasicAuth(HttpServletRequest _req) {
        String u = null;
        String p = null;
        String auth = _req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Basic")) {
            String credentials = new String(Base64.decodeBase64(auth.substring("Basic".length()).trim()), StandardCharsets.UTF_8);
            String[] values = credentials.split(":", 2);
            if (values.length == 2) {
                u = values[0];
                p = values[1];
            }
        }
        username = u;
        password = p;
    }

    public BasicAuth(String _username, String _password) {
        username = _username;
        password = _password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static String toHeader(String _username, String _password) {
        return "Basic " + Base64.encodeBase64String(NullUtils.toByteArray(_username + ":" + _password));
    }
}