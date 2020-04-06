package com.vwmin.restproxy;

import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;

/**
 * @author vwmin
 * @version 1.0
 * @date 2020/4/6 11:18
 */
public class RestServiceMethod<T> {

    public static RestServiceMethod<?> genImplement(String baseUrl, RestTemplate restTemplate, Method method) {
        RestRequestFactory factory = RestRequestFactory.parseAnnotations(baseUrl, method);
        return new RestServiceMethod<>(factory, restTemplate);
    }

    private RestRequestFactory restRequestFactory;
    private RestTemplate restTemplate;

    private RestServiceMethod(RestRequestFactory restRequestFactory, RestTemplate restTemplate){
        this.restRequestFactory = restRequestFactory;
        this.restTemplate = restTemplate;
    }

    T invoke(Object[] args) throws Throwable{
        return new RestCall<T>(restRequestFactory, args, restTemplate).execute();
    }
}
