// Copyright (C) 2019 Meituan
// All rights reserved
package com.csonezp.server.http;

import com.google.common.collect.Maps;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author zhangpeng34
 * Created on 2019/4/15 下午4:43
 **/
@Slf4j
public class RequestParamsParser {

    public static Map<String, Object> parse(FullHttpRequest fullReq) {
        HttpMethod method = fullReq.method();
        Map<String, Object> parmMap = Maps.newHashMap();
        if (HttpMethod.GET == method) {
            QueryStringDecoder decoder = new QueryStringDecoder(fullReq.uri());
            if (null != decoder) {
                List<String> valueList;
                for (Map.Entry<String, List<String>> entry : decoder.parameters().entrySet()) {
                    valueList = entry.getValue();
                    if (null != valueList) {
                        parmMap.put(entry.getKey(), 1 == valueList.size() ? valueList.get(0) : valueList);
                    }
                }
            }
        } else if (HttpMethod.POST == method) {
            HttpPostRequestDecoder decoder = null;
            try {
                decoder = new HttpPostRequestDecoder(fullReq);
                decoder.offer(fullReq);
                List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();

                for (InterfaceHttpData parm : parmList) {

                    Attribute data = (Attribute) parm;
                    try {
                        parmMap.put(data.getName(), data.getValue());
                    } catch (IOException e) {
                        log.error("put param error!", e);
                    }

                }
            } finally {
                if (null != decoder) {
                    decoder.destroy();
                }
            }


        } else {
            // 不支持其它方法
            throw new RuntimeException("Method Not Support!"); // 这是个自定义的异常, 可删掉这一行
        }
        return parmMap;
    }
}