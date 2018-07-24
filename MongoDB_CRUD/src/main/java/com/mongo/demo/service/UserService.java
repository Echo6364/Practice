package com.mongo.demo.service;

import com.mongo.demo.model.User;
import org.springframework.data.domain.Pageable;
import java.util.List;

//Service 接口类

public interface UserService {
    List<User> findAll();

    User getUser(Integer id);

    void update(User user);

    void insert(User user);

    void insertAll(List<User> user);

    void remove(Integer id);

    List<User> findByPage(User user, Pageable pageable);
}
