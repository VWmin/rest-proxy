package com.vwmin.restproxy;

import com.vwmin.restproxy.annotations.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

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

    /** URI 正则*/
    private static final Pattern QUERY_PARAM_PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");
    private static final Pattern QUERY_PATTERN = Pattern.compile("(\\?([^#]*))?");


    /** 请求参数控制*/
    private boolean gotJson = false;
    private boolean gotBody = false;

    /** 额外的功能标记 */
    private boolean logRequest = false;

    private HttpHeaders headers;


    public RestRequestFactoryBuilder(final String baseUrl, final Method serviceMethod){
        this.baseUrl = baseUrl;
        this.serviceMethod = serviceMethod;
        this.methodAnnotations = serviceMethod.getAnnotations();
        this.parameterAnnotationsArray = serviceMethod.getParameterAnnotations();
        this.parameterTypes = serviceMethod.getGenericParameterTypes();
        this.headers = new HttpHeaders();

    }


    public RestRequestFactory build() {

        // 解析参数注解中的HTTP注解，主要是Query、Body等参数
        int paramCnt = parameterAnnotationsArray.length;
        Annotation[] parameterAnnotations = new Annotation[paramCnt];
        for(int i=0; i<paramCnt; i++){
            parameterAnnotations[i] = parseParameterAnnotations(i, parameterAnnotationsArray[i]);
        }

        // 解析方法注解中的HTTP注解、主要是GET、POST等参数
        parseMethodAnnotations(methodAnnotations);

        //通过已知的参数类型，设置header的content-type
        if (gotBody){
            headers.add("content-type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        }else if (gotJson){
            headers.add("content-type", MediaType.APPLICATION_JSON_VALUE);
        }

        RestRequestFactory factory = new RestRequestFactory(
                url,
                httpMethod,
                parameterAnnotations,
                serviceMethod.getReturnType(),
                serviceMethod,
                headers
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
            }else if (annotation instanceof LogRequest){ //标记该方法是否需要打印请求日志
                logRequest = true;
            }else if (annotation instanceof Headers){
                for( Header header : ((Headers) annotation).headers() ){
                    headers.add(header.k(), header.v());
                }
            }else if (annotation instanceof Header){
                Header header = (Header) annotation;
                headers.add(header.k(), header.v());
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
            Annotation get;
            //筛选出参数注解中的HTTP注解
            if (annotation instanceof Query || annotation instanceof Path){
                get = annotation;
            }else if (annotation instanceof Json){
                if (gotJson){
                    throw Utils.parameterError(serviceMethod, index, "只允许有一个@Json参数");
                }
                if (gotBody){
                    throw Utils.parameterError(serviceMethod, index, "不能与@Body参数同时存在");
                }
                get = annotation;
                gotJson = true;
            }else if (annotation instanceof Field){
                if (gotJson){
                    throw Utils.parameterError(serviceMethod, index, "不能与@Json参数同时存在");
                }
                get = annotation;
                gotBody = true;
            }else if (annotation instanceof Body) {
                if (gotJson){
                    throw Utils.parameterError(serviceMethod, index, "不能与@Json参数同时存在");
                }
                get = annotation;
                gotBody = true;
            }else { // TODO: 2020/4/6 添加对其它参数形式的支持
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
