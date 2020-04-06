package com.vwmin.restproxy;

import com.vwmin.restproxy.annotations.GET;
import com.vwmin.restproxy.annotations.Query;
import org.springframework.http.HttpMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.regex.Pattern;


/**
 * @author vwmin
 * @version 1.0
 * @date 2020/4/6 12:01
 */
public class RestRequestFactoryBuilder {
    private final Method serviceMethod;
    private final Annotation[] methodAnnotations;
    private final Annotation[][] parameterAnnotationsArray;
    private final Type[] parameterTypes;


    /** URI 参数*/
    private final String baseUrl;
    private String url;
    private HttpMethod httpMethod = null;
    private Annotation[] parameterAnnotations;

    /** URI 正则*/
    private static final Pattern QUERY_PARAM_PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");
    private static final Pattern QUERY_PATTERN = Pattern.compile("(\\?([^#]*))?");


    public RestRequestFactoryBuilder(final String baseUrl, final Method serviceMethod){
        this.baseUrl = baseUrl;
        this.serviceMethod = serviceMethod;
        this.methodAnnotations = serviceMethod.getAnnotations();
        this.parameterAnnotationsArray = serviceMethod.getParameterAnnotations();
        this.parameterTypes = serviceMethod.getGenericParameterTypes();

    }


    public RestRequestFactory build() {

        //解析参数注解中的HTTP注解
        int paramCnt = parameterAnnotationsArray.length;
        parameterAnnotations = new Annotation[paramCnt];
        for(int i=0; i<paramCnt; i++){
            parameterAnnotations[i] = parseParameterAnnotations(i, parameterAnnotationsArray[i]);
        }

        // 解析方法注解中的HTTP注解
        parseMethodAnnotations(methodAnnotations);

        return new RestRequestFactory(
                url,
                httpMethod,
                parameterAnnotations,
                serviceMethod.getReturnType(),
                serviceMethod
        );

    }

    private void parseMethodAnnotations(Annotation[] annotations) {
        for (Annotation annotation : annotations){
            if (annotation instanceof GET){
                setHttpMethodAndUrl(HttpMethod.GET, ((GET) annotation).value());
            }
            // TODO: 2020/4/6 添加对其它HTTP方法的支持
        }

        if (httpMethod == null){
            throw Utils.methodError(serviceMethod, "需要至少一个HTTP方法注解.");
        }
    }

    /**
     * 主要是过滤参数上的非HTTP注解
     * @param annotations 参数上的注解s
     * @return 唯一的HTTP注解
     */
    private Annotation parseParameterAnnotations(int index, Annotation[] annotations) {
        Annotation result = null;
        for (Annotation annotation : annotations){
            Annotation get = null;
            if (annotation instanceof Query){
                get = annotation;
            }// TODO: 2020/4/6 添加对其它参数形式的支持


            if (get == null){
                continue;
            }

            if (result == null){
                result = get;
            }else {
                throw Utils.parameterError(serviceMethod, index, "只允许有一个HTTP参数注解，收到%s，已存在%s", get, annotation);
            }
        }
        return result;
    }

    private void setHttpMethodAndUrl(HttpMethod httpMethod, String relativePath) {
        if (this.httpMethod != null){
            throw Utils.methodError(serviceMethod,
                    "只允许有一个HTTP方法注解，收到%s，已存在%s.", httpMethod.name(), this.httpMethod.name());
        }
        Utils.notNull(serviceMethod, relativePath, "相对路径不能为空");

        this.httpMethod = httpMethod;
        this.url = baseUrl + relativePath;
    }

}
