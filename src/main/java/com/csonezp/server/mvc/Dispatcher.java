// Copyright (C) 2019 Meituan
// All rights reserved
package com.csonezp.server.mvc;

import com.csonezp.server.annotation.Controller;
import com.csonezp.server.annotation.RequestMapping;
import com.csonezp.server.annotation.RequestParam;
import com.csonezp.server.http.Request;
import com.csonezp.server.http.Response;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author zhangpeng34
 * Created on 2019/4/15 下午5:08
 **/
@Slf4j
public class Dispatcher {
    private static List<String> classNames = Lists.newArrayList();
    private static Map<String, Object> ioc = Maps.newHashMap();
    private static Map<String, Method> handlerMapping = Maps.newHashMap();
    private static Map<String, Object> controllerMap = Maps.newHashMap();

    public void init(String packageToScan) {
        //1，扫包
        scanPackage(packageToScan);

        //2、ioc
        iocInit();

        //3、handlerMapping
        handlerMappingInit();
    }

    private void handlerMappingInit() {
        if (ioc.isEmpty()) {
            return;
        }
        try {
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                Class<? extends Object> clazz = entry.getValue().getClass();
                if (!clazz.isAnnotationPresent(Controller.class)) {
                    continue;
                }

                String baseUrl = "";
                if (clazz.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping annotation = clazz.getAnnotation(RequestMapping.class);
                    baseUrl = annotation.value();
                }
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(RequestMapping.class)) {
                        continue;
                    }
                    RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                    String url = annotation.value();

                    url = (baseUrl + "/" + url).replaceAll("/+", "/");
                    //url 和method的对应
                    handlerMapping.put(url, method);
                    //url 和 controller实例的对应
                    controllerMap.put(url, clazz.newInstance());
                    System.out.println(url + "," + method);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void iocInit() {
        if (classNames.isEmpty()) {
            return;
        }
        for (String className : classNames) {
            try {
                //反射来实例化(只有加@Controller需要实例化)
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    ioc.put(toLowerFirstWord(clazz.getSimpleName()), clazz.newInstance());
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private void scanPackage(String packageToScan) {
        URL url = this.getClass().getClassLoader().getResource(packageToScan.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                //递归读取包
                scanPackage(packageToScan + "." + file.getName());
            } else {
                String className = packageToScan + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * 把字符串的首字母小写
     *
     * @param name
     * @return
     */
    private String toLowerFirstWord(String name) {
        char[] charArray = name.toCharArray();
        charArray[0] += 32;
        return String.valueOf(charArray);
    }


    public String doDisPatch(Request request) {
        if (handlerMapping.isEmpty()) {
            return null;
        }
        String url = request.getPath();
        if (!this.handlerMapping.containsKey(url)) {
            return null;
        }

        Method method = this.handlerMapping.get(url);

        Object[] paramValues = processParams(method, request.getParams());

        try {
            //调用
            Object resp = method.invoke(this.controllerMap.get(url), paramValues);//obj是method所对应的实例 在ioc容器中
            return resp.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 拼装参数列表
     */
    private Object[] processParams(Method method, Map<String, Object> params) {
        //方法签名的参数列表
        Parameter[] methodParams = method.getParameters();

        //反射所用的参数值列表
        Object[] paramValues = new Object[methodParams.length];

        //给参数赋值
        for (int i = 0; i < methodParams.length; i++) {
            try {
                Parameter parameter = methodParams[i];
                if (parameter.isAnnotationPresent(RequestParam.class)) {
                    RequestParam param = parameter.getAnnotation(RequestParam.class);
                    String name = param.value();
                    String value = params.get(name).toString();
                    paramValues[i] = value;
                }
            } catch (Exception e) {
                paramValues[i] = null;
                e.printStackTrace();
            }

        }

        return paramValues;
    }
}