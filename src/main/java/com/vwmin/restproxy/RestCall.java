package com.vwmin.restproxy;

import org.springframework.http.HttpMethod;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @author vwmin
 * @version 1.0
 * @date 2020/4/6 11:36
 */
public class RestCall<T> {
    private RestRequestFactory restRequestFactory;
    private RestTemplate restTemplate;
    private Object[] args;

    public RestCall(RestRequestFactory restRequestFactory, Object[] args, RestTemplate restTemplate) {
        this.restRequestFactory = restRequestFactory;
        this.restTemplate = restTemplate;
        this.args = args;
    }

    public T execute() throws Throwable{
        URI url = restRequestFactory.create(args);
        HttpMethod httpMethod = restRequestFactory.getHttpMethod();
        RequestCallback requestCallback = restRequestFactory.requestCallback(restTemplate);
        ResponseExtractor<T> responseExtractor = restRequestFactory.responseExtractor(restTemplate);
        return restTemplate.execute(url, httpMethod, requestCallback, responseExtractor);
    }
}
