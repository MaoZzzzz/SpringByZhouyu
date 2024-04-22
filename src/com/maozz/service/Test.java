package com.maozz.service;

import com.maozz.spring.MySpringApplicationContext;

public class Test {
    public static void main(String[] args) {

        MySpringApplicationContext mySpringApplicationContext = new MySpringApplicationContext(AppConfig.class);

        UserService userService = (UserService) mySpringApplicationContext.getBean("userService");
        userService.test();
    }
}
