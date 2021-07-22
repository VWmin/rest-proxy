package com.vwmin.restproxy.demo;

import com.vwmin.restproxy.RestProxy;
import com.vwmin.restproxy.annotations.Field;
import com.vwmin.restproxy.annotations.POST;
import org.springframework.web.client.RestTemplate;

/**
 * @author vwmin
 * @version 1.0
 * @date 2021/1/16 21:02
 */
public class Test {
    static class Person{
        public String name;
        public Person(String name){
            this.name = name;
        }
    }
    interface TestApi{
        @POST("/person")
        String post(@Field("name") String name, @Field("age") int age);
    }

    public static void main(String[] args) {
        TestApi api = new RestProxy<>("http://localhost", TestApi.class, new RestTemplate()).getApi();
        System.out.println(api.post("yyy", 18));
    }
}
