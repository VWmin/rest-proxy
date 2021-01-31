package com.vwmin.restproxy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author vwmin
 * @version 1.0
 * @date 2020/4/7 12:05
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Json {
    /**
     * Json参数的name，实际并未参与到请求中
     */
    String value() default "";
    boolean required() default true;
}
