package com.vwmin.restproxy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author vwmin
 * @version 1.0
 * @date 2021/1/16 20:35
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Headers {
    Header[] headers() default {};
}
