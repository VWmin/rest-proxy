package com.vwmin.restproxy;

import com.vwmin.restproxy.annotations.*;
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


    /** 请求参数控制*/
    private boolean gotBody = false;

    /** 额外的功能标记 */
    private boolean logRequest = false;


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

        RestRequestFactory factory = new RestRequestFactory(
                url,
                httpMethod,
                parameterAnnotations,
                serviceMethod.getReturnType(),
                serviceMethod
        );

        if (logRequest) {
            factory.setLogRequest(true);
        }

        return factory;
    }

    private void parseMethodAnnotations(Annotation[] annotations) {
        for (Annotation annotation : annotations){
            if (annotation instanceof GET){
                setHttpMethodAndUrl(HttpMethod.GET, ((GET) annotation).value());
            }else if (annotation instanceof POST){
                setHttpMethodAndUrl(HttpMethod.POST, ((POST) annotation).value());
            }else if (annotation instanceof LogRequest){
                logRequest = true;
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
            }else if (annotation instanceof Body){
                if (gotBody){
                    //todo 不知道是不是该这样
                    throw Utils.parameterError(serviceMethod, index, "只允许有一个@Body参数");
                }
                get = annotation;
                gotBody = true;
            }


            // TODO: 2020/4/6 添加对其它参数形式的支持


            if (get == null){
                continue;
            }

            if (result == null){
                result = get;
            }else {
                throw Utils.parameterError(serviceMethod, index, "一个参数只允许有一个HTTP参数注解，收到%s，已存在%s", get, annotation);
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
