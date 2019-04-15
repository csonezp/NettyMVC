// Copyright (C) 2019 Meituan
// All rights reserved
package com.csonezp.server.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhangpeng34
 * Created on 2019/4/15 下午5:11
 **/
@Slf4j
public class Response {
    public final static String CONTENT_TYPE_JSON = "application/json";
    private String contentType = CONTENT_TYPE_JSON;
    private String charset = "utf-8";
    private HttpResponseStatus status = HttpResponseStatus.OK;



    private ChannelHandlerContext ctx;
    private Object content = Unpooled.EMPTY_BUFFER;
    private HttpVersion httpVersion = HttpVersion.HTTP_1_1;
    private HttpHeaders headers = new DefaultHttpHeaders();
    private Set<io.netty.handler.codec.http.cookie.Cookie> cookies = new HashSet<Cookie>();




    public Response(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public Response setContent(String contentText) {
        this.content = Unpooled.copiedBuffer(contentText, Charset.forName(charset));
        return this;
    }

    public ChannelFuture write() {
        return ctx.writeAndFlush(toFullHttpResponse()).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 转换为Netty所用Response<br>
     * 用于返回一般类型响应（文本）
     *
     * @return FullHttpResponse
     */
    private FullHttpResponse toFullHttpResponse() {
        final ByteBuf byteBuf = (ByteBuf) content;
        final FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(httpVersion, status, byteBuf);

        // headers
        final HttpHeaders httpHeaders = fullHttpResponse.headers().add(headers);
        httpHeaders.set(HttpHeaderNames.CONTENT_TYPE.toString(), String.format("{};charset={}", contentType, charset));
        httpHeaders.set(HttpHeaderNames.CONTENT_ENCODING.toString(), charset);
        httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH.toString(), byteBuf.readableBytes());

        // Cookies
        for (io.netty.handler.codec.http.cookie.Cookie cookie : cookies) {
            httpHeaders.add(HttpHeaderNames.SET_COOKIE.toString(), ServerCookieEncoder.LAX.encode(cookie));
        }

        return fullHttpResponse;
    }

}