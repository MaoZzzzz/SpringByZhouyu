package com.maozz.service;

import com.maozz.spring.Autowired;
import com.maozz.spring.Component;

@Component("userService")
public class UserService {

    @Autowired
    private OrderService orderService;

    public void test() {
        System.out.println(orderService);
    }
}
