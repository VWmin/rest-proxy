package com.vwmin.restproxy;

import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author vwmin
 * @version 1.0
 * @date 2020/4/6 11:12
 */
public class RestProxy<API> {
    private final String baseUrl;
    private final Map<Method, RestServiceMethod<?>> methodCache = new ConcurrentHashMap<>();
    private API api;
    private RestTemplate restTemplate;

    public RestProxy(String url, Class<API> restService, RestTemplate restTemplate){
        this.baseUrl = url;
        this.restTemplate = restTemplate;
        this.api = create(restService);
    }

    public API getApi(){
        return api;
    }

    @SuppressWarnings("unchecked")
    private API create(Class<API> restService){
        return (API) Proxy.newProxyInstance(
                restService.getClassLoader(),
                new Class<?>[]{restService},
                (proxy, method, args) -> loadServiceMethod(method).invoke(orEmpty(args))
        );
    }

    private RestServiceMethod<?> loadServiceMethod(Method method) {
        RestServiceMethod<?> restServiceMethod = methodCache.get(method);
        if (restServiceMethod != null){
            return restServiceMethod;
        }
        restServiceMethod = RestServiceMethod.genImplement(baseUrl, restTemplate, method);
        methodCache.put(method, restServiceMethod);
        return restServiceMethod;
    }

    private Object[] orEmpty(Object[] args){
        return args != null ? args : new Object[0];
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
