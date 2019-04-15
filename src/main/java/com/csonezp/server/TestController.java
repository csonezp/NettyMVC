// Copyright (C) 2019 Meituan
// All rights reserved
package com.csonezp.server;

import com.csonezp.server.annotation.Controller;
import com.csonezp.server.annotation.RequestMapping;
import com.csonezp.server.annotation.RequestParam;

/**
 * @author zhangpeng34
 * Created on 2019/4/15 下午9:36
**/
@Controller
public class TestController {
    @RequestMapping("/test")
    public String test(@RequestParam("name") String name,@RequestParam("age")String age){
        return name+" hello! age:"+age;
    }
}