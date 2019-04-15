// Copyright (C) 2019 Meituan
// All rights reserved
package com.csonezp.server.http;

import com.sun.xml.internal.ws.util.UtilException;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author zhangpeng34
 * Created on 2019/4/15 下午4:30
 **/
public class Request {
    private FullHttpRequest request;
    private String path;
    private Map<String, String> headers = new HashMap<String, String>();
    private Map<String, Object> params = new HashMap<String, Object>();
    private Map<String, Cookie> cookies = new HashMap<String, Cookie>();

    public Request(FullHttpRequest fullHttpRequest) {
        request = fullHttpRequest;
        String uri = request.uri();
        path = getPathFromUri(uri);
        initHeaders(request);
        initCookies(request);
        initParams(request);
    }

    private void initParams(FullHttpRequest request) {
        params = RequestParamsParser.parse(request);
    }


    /**
     * 初始化cookie
     */
    private void initCookies(FullHttpRequest request) {
        String cookieStr = headers.get(HttpHeaderNames.COOKIE.toString());
        if (StringUtils.isNotBlank(cookieStr)) {
            Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieStr);
            for (Cookie cookie : cookies) {
                this.cookies.put(cookie.name(), cookie);
            }
        }
    }

    /**
     * 初始化headers
     * @param request
     */
    private void initHeaders(FullHttpRequest request) {
        for (Map.Entry<String, String> entry : request.headers()) {
            this.headers.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 获取url path
     */
    public static String getPathFromUri(String uriStr) {
        URI uri = null;
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException e) {
            throw new UtilException(e);
        }
        return uri.getPath();
    }

    public String getPath() {
        return path;
    }

    /**
     * @return 是否为长连接
     */
    public boolean isKeepAlive() {
        final String connectionHeader = getHeader(HttpHeaderNames.CONNECTION.toString());
        // 无论任何版本Connection为close时都关闭连接
        if (HttpHeaderValues.CLOSE.toString().equalsIgnoreCase(connectionHeader)) {
            return false;
        }

        // HTTP/1.0只有Connection为Keep-Alive时才会保持连接
        if (HttpVersion.HTTP_1_0.text().equals(getProtocolVersion())) {
            if (false == HttpHeaderValues.KEEP_ALIVE.toString().equalsIgnoreCase(connectionHeader)) {
                return false;
            }
        }
        // HTTP/1.1默认打开Keep-Alive
        return true;
    }
    public String getHeader(String headerKey) {
        return headers.get(headerKey);
    }
    public String getProtocolVersion() {
        return request.protocolVersion().text();
    }

    public Map<String, Object> getParams() {
        return params;
    }
}