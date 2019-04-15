package com.csonezp.server.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
	/**
     * 表示访问该方法的url
     * @return
     */
    String value() default "";

}
