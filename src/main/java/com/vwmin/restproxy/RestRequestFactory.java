package com.vwmin.restproxy;

import com.vwmin.restproxy.annotations.Body;
import com.vwmin.restproxy.annotations.Json;
import com.vwmin.restproxy.annotations.Path;
import com.vwmin.restproxy.annotations.Query;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author vwmin
 * @version 1.0
 * @date 2020/4/6 11:31
 */
public class RestRequestFactory {

    private static final Log logger = LogFactory.getLog(RestRequestFactory.class);

    private final String url;
    private final HttpMethod httpMethod;
    private final Annotation[] parameterAnnotations;
    private final Class<?> returnType;
    private final Method serviceMethod;
    private Object requestEntity;
    private final HttpHeaders headers;

    private boolean logRequest = false;

    public static RestRequestFactory parseAnnotations(String baseUrl, Method serviceMethod) {
        return new RestRequestFactoryBuilder(baseUrl, serviceMethod).build();
    }

    RestRequestFactory(
            String url,
            HttpMethod httpMethod,
            Annotation[] parameterAnnotations,
            Class<?> returnType,
            Method serviceMethod,
            HttpHeaders headers
    ){
        this.url = url;
        this.httpMethod = httpMethod;
        this.parameterAnnotations = parameterAnnotations;
        this.returnType = returnType;
        this.serviceMethod = serviceMethod;
        this.headers = headers;
    }


    public URI create(Object[] args) {
        //为Query服务
        Map<String, Object> uriVariables = new HashMap<>(args.length);
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

        //为Body服务
        MultiValueMap<String, Object> bodies = new LinkedMultiValueMap<>();


        for (int i=0; i<args.length; i++){
            Annotation annotation = parameterAnnotations[i];
            Object arg = args[i];

            //Query
            if (annotation instanceof Query){
                String queryName = ((Query) annotation).value();
                if (arg == null){
                    if (((Query) annotation).required()){
                        throw Utils.parameterError(serviceMethod, i, "不能为空的Query参数(%s)！", queryName);
                    }else {
                        continue;
                    }
                }
                uriVariables.put(queryName, arg);
                uriComponentsBuilder.query(String.format("%s={%s}", queryName, queryName));
            }

            //Path
            else if (annotation instanceof Path){
                String paramName = ((Path) annotation).value();
                if (arg == null){
                    throw Utils.parameterError(serviceMethod, i, "Path参数(%s)不能为空！", paramName);
                }
                uriVariables.put(paramName, arg);
            }

            //Json application/json
            else if (annotation instanceof Json){
                String queryName = ((Json) annotation).value();
                if (arg == null){
                    if (((Json) annotation).required()){
                        throw Utils.parameterError(serviceMethod, i, "不能为空的Json参数(%s)！", queryName);
                    }else {
                        continue;
                    }
                }
                this.requestEntity = arg;
            }

            //Body  与Json冲突  application/x-www-form-urlencoded
            else if (annotation instanceof Body){
                String paramName = ((Body) annotation).value();
                if (arg == null){
                    if (((Body) annotation).required()){
                        throw Utils.parameterError(serviceMethod, i, "不能为空的Body参数(%s)！", paramName);
                    }else {
                        continue;
                    }
                }
                bodies.add(paramName, arg);
                this.requestEntity = bodies;
            }

        }


        URI uri = uriComponentsBuilder.build(uriVariables);

        if (logRequest){
            logger.info("going to request >>> " + uri.toString());
        }

        return uri;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public RequestCallback requestCallback(RestTemplate restTemplate) {
        switch (httpMethod){
            case GET:
                return restTemplate.acceptHeaderRequestCallback(returnType);
            case POST:
                return restTemplate.httpEntityCallback(new HttpEntity<>(requestEntity, headers), returnType);
            default:
                return restTemplate.acceptHeaderRequestCallback(returnType);

        }
    }

    public <T> ResponseExtractor<T> responseExtractor(RestTemplate restTemplate) {
        return new HttpMessageConverterExtractor<T>(returnType, restTemplate.getMessageConverters());
    }

    public void setLogRequest(boolean logRequest) {
        this.logRequest = logRequest;
    }
}
