package com.mongo.demo.controller;

import com.mongo.demo.model.User;
import com.mongo.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")

//web测试 SpringApplication加载  运行  localhost:4004/user/add
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/get/{id}")
    public User getUser(@PathVariable int id) {
        return userService.getUser(id);
    }

    @RequestMapping("/delete/{id}")
    public String delete(@PathVariable int id) {
        userService.remove(id);
        return "delete sucess";
    }
    @RequestMapping("/add")
    public String insert() {
        User user = new User(221312, "zard", 16);
        userService.insert(user);
        return "sucess";
    }

    @RequestMapping("/insert")
    public String insertAll() {
        List<User> list = new ArrayList<>();
        for (int i = 10; i < 15; i++) {
            list.add(new User(i, "" + i, i));
        }
        userService.insertAll(list);
        return "sucess";
    }

    @RequestMapping("/find/all")
    public List<User> find() {
        return userService.findAll();
    }

    @RequestMapping("/find/{start}")
    public List<User> findByPage(@PathVariable int start, User user) {
        Pageable pageable = new PageRequest(start, 2);
        return userService.findByPage(user, pageable);
    }

    @RequestMapping("/update/{id}")
    public String update(@PathVariable int id) {
        User user = new User(id, "" + 1, 1);
        userService.update(user);
        return "sucess";
    }
}