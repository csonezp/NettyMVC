// Copyright (C) 2019 Meituan
// All rights reserved
package com.csonezp.server.netty;

import com.csonezp.server.http.Request;
import com.csonezp.server.http.Response;
import com.csonezp.server.mvc.Dispatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangpeng34
 * Created on 2019/4/15 下午4:28
 **/
@Slf4j
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    Dispatcher dispatcher;

    public HttpRequestHandler(Dispatcher dispatcher){
        super();
        this.dispatcher = dispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        Request request = new Request(fullHttpRequest);
        Response response = new Response(channelHandlerContext);
        //todo 过滤器部分

        //模仿spring Dispatcher,执行请求
        doDispatch(request, response);



    }

    private void doDispatch(Request request, Response response) {
        String resp = dispatcher.doDisPatch(request);
        response.setContent(resp);
        response.write();
    }
}