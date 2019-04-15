package com.csonezp.server.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
	/**
     * 表示给controller注册别名
     * @return
     */
    String value() default "";

}
