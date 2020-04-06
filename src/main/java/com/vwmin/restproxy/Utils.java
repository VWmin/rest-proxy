package com.vwmin.restproxy;

import java.lang.reflect.Method;

/**
 * @author vwmin
 * @version 1.0
 * @date 2020/4/6 12:40
 */
class Utils {
    private Utils(){}

    static RuntimeException methodError(Method method, String message, Object... args) {
        message = String.format(message, args);
        return new IllegalArgumentException(
                message
                + "\n    for method "
                + method.getDeclaringClass().getSimpleName()
                + "."
                + method.getName()
        );
    }

    static RuntimeException parameterError(Method method, int index, String message, Object... args) {
        return methodError(method, message + " (parameter #" + (index + 1) + ")", args);
    }

    static void notNull(Method method, Object object, String message, Object... args){
        if (object == null){
            throw methodError(method, message, args);
        }
    }
}
